/*
 * Copyright 2019 ukuz90
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.ukuz.piccolo.client.id.snowflake;

import io.github.ukuz.piccolo.api.common.remote.FailoverInvoker;
import io.github.ukuz.piccolo.api.common.threadpool.ExecutorFactory;
import io.github.ukuz.piccolo.api.connection.Connection;
import io.github.ukuz.piccolo.api.external.common.Assert;
import io.github.ukuz.piccolo.api.id.IdGen;
import io.github.ukuz.piccolo.api.id.IdGenException;
import io.github.ukuz.piccolo.api.loadbalance.LoadBalancer;
import io.github.ukuz.piccolo.api.service.discovery.ServiceInstance;
import io.github.ukuz.piccolo.api.spi.SpiLoader;
import io.github.ukuz.piccolo.client.PiccoloClient;
import io.github.ukuz.piccolo.client.id.IdGenManager;
import io.github.ukuz.piccolo.client.id.UniqueIdGen;
import io.github.ukuz.piccolo.common.ServiceNames;
import io.github.ukuz.piccolo.common.message.IdGenMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author ukuz90
 */
public class SnowflakeIdGenDelegate implements IdGen, UniqueIdGen {

    public static final String INIT_TAG = "f";
    private static Logger LOGGER = LoggerFactory.getLogger(SnowflakeIdGenDelegate.class);
    private static final int DEFAULT_BUFFER_SIZE = 10000;
    private static final int DEFAULT_THRESHOLD = 6000;
    private static final double DEFAULT_FACTOR = 0.6d;
    private final int capacity;
    private final IdBuffer idBuffer1;
    private final IdBuffer idBuffer2;
    private ExecutorService executor;
    private PiccoloClient piccoloClient;
    private final AtomicBoolean sending;
    private final AtomicBoolean started = new AtomicBoolean(false);
    private final Long id;

    public SnowflakeIdGenDelegate(PiccoloClient piccoloClient) {
        this(piccoloClient, DEFAULT_BUFFER_SIZE, DEFAULT_THRESHOLD);
    }

    public SnowflakeIdGenDelegate(PiccoloClient piccoloClient, int capacity) {
        this(piccoloClient, capacity, (int) (capacity * DEFAULT_FACTOR));
    }

    public SnowflakeIdGenDelegate(PiccoloClient piccoloClient, int capacity, int threshold) {
        Assert.notNull(piccoloClient, "piccoloClient must not be null");
        if (capacity <= 0) {
            capacity = DEFAULT_BUFFER_SIZE;
        }
        if (threshold <= 0) {
            threshold = DEFAULT_THRESHOLD;
        }
        capacity = Math.min(capacity, Integer.MAX_VALUE);
        if (threshold > capacity) {
            threshold = capacity;
        }
        this.capacity = capacity;
        idBuffer1 = new IdBuffer(capacity, threshold);
        idBuffer2 = new IdBuffer(capacity, threshold);

        this.piccoloClient = piccoloClient;
        this.sending = new AtomicBoolean(false);

        id = IdGenManager.getInstance().acquireId();
        IdGenManager.getInstance().register(this);
    }

    @Override
    public boolean init() throws IllegalStateException {
        if (started.compareAndSet(false, true)) {
            executor = (ExecutorService) piccoloClient.getExecutorFactory().create(ExecutorFactory.ID_GEN, piccoloClient.getEnvironment());
            getXidAsync(true);
            return true;
        } else {
            throw new IllegalStateException("initialize duplicate");
        }
    }

    @Override
    public boolean destroy() {
        if (started.compareAndSet(true, false)) {
            executor.shutdown();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public long get(String tag) throws IdGenException {
        if (!started.get()) {
            throw new IdGenException("IdGen not initialized");
        }
        long xid = 0;
        try {
            xid = masterBuffer().read();
            if (xid <= 0) {
                throw new IdGenException("no available xid");
            }
        } catch (IdGenException e) {
            try {
                TimeUnit.MILLISECONDS.sleep(10);
                xid = masterBuffer().read();
            } catch (InterruptedException e1) {
                throw e;
            }
        }

        if (masterBuffer().isWarn()) {
            getXidAsync(false);
        }
        return xid;
    }

    public synchronized void writeData(long[] data, boolean isInit) {
        if (!started.get()) {
            LOGGER.error("IdGen not initialized");
            return;
        }
        try {
            if (isInit) {
                masterBuffer().write(data);
            } else {
                backupBuffer().write(data);
            }
        } catch (Exception e) {
            LOGGER.error("writeData failure, cause: {}", e);
            e.printStackTrace();
        } finally {
            sending.compareAndSet(true, false);
        }

    }


    private IdBuffer masterBuffer() {
        return idBuffer1.canNotRead() ? idBuffer2 : idBuffer1;
    }

    private IdBuffer backupBuffer() {
        return idBuffer1.canNotRead() ? idBuffer1 : idBuffer2;
    }

    private void getXidAsync(boolean isInit) {
        if (sending.compareAndSet(false, true)) {
            if (!isInit && !backupBuffer().canNotRead()) {
                sending.compareAndSet(true, false);
                return;
            }
            executor.execute(()-> {
                List<ServiceInstance> serviceInstances = piccoloClient.getServiceDiscovery().lookup(ServiceNames.S_GATEWAY);
                LoadBalancer loadBalancer = SpiLoader.getLoader(LoadBalancer.class).getExtension();
                LOGGER.info("get xid async, init: {}", isInit);
                FailoverInvoker invoker = new FailoverInvoker();
                try {
                    invoker.invoke(() -> {
                        ServiceInstance si = loadBalancer.choose(serviceInstances);
                        try {
                            Connection connection = piccoloClient.getGatewayConnectionFactory().getConnection(si.getHostAndPort());
                            if (connection != null) {
                                IdGenMessage idGenMessage = new IdGenMessage(connection);
                                idGenMessage.tag = isInit ? INIT_TAG : null;
                                idGenMessage.batchSize = (short)capacity;
                                idGenMessage.id = id;
                                connection.sendAsync(idGenMessage, future -> {
                                    if (!future.isSuccess()) {
                                        LOGGER.warn("get xid async send failure, cause: {}", future.cause());
                                        sending.set(false);
                                    }
                                });
                            } else {
                                LOGGER.error("can not get id to gateway server, is it work, server: {}",
                                        si.getHostAndPort());
                            }

                        } catch (Exception e) {
                            serviceInstances.remove(si);
                            throw e;
                        }
                        return null;
                    });
                } catch (Exception e) {
                    LOGGER.error("get xid async failure, cause: {}", e);
                    sending.set(false);
                }
            });
        }
    }


    @Override
    public Long getId() {
        return id;
    }
}
