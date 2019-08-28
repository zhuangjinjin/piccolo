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
import io.github.ukuz.piccolo.common.ServiceNames;
import io.github.ukuz.piccolo.common.message.IdGenMessage;
import io.github.ukuz.piccolo.registry.zookeeper.ZKRegistration;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author ukuz90
 */
public class SnowflakeIdGenDelegate implements IdGen {

    public static final String INIT_TAG = "f";
    private static Logger LOGGER = LoggerFactory.getLogger(SnowflakeIdGenDelegate.class);
    private static final int BUFFER_SIZE = 10000;
    private IdBuffer idBuffer1 = new IdBuffer(BUFFER_SIZE, 6000);
    private IdBuffer idBuffer2 = new IdBuffer(BUFFER_SIZE, 6000);
    private ExecutorService executor;
    private PiccoloClient piccoloClient;
    private final AtomicBoolean sending;

    public SnowflakeIdGenDelegate(PiccoloClient piccoloClient) {
        Assert.notNull(piccoloClient, "piccoloClient must not be null");
        this.piccoloClient = piccoloClient;
        this.sending = new AtomicBoolean(false);
        executor = (ExecutorService) piccoloClient.getExecutorFactory().create(ExecutorFactory.ID_GEN, piccoloClient.getEnvironment());
    }

    @Override
    public boolean init() {
        getXidAsync(true);
        return true;
    }

    @Override
    public boolean destroy() {
        executor.shutdown();
        return true;
    }

    @Override
    public long get(String tag) throws IdGenException {
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
                List<ZKRegistration> serviceInstances = piccoloClient.getServiceDiscovery().lookup(ServiceNames.S_GATEWAY);
                LoadBalancer loadBalancer = SpiLoader.getLoader(LoadBalancer.class).getExtension();
                LOGGER.info("get xid async, init: {}", isInit);
                FailoverInvoker invoker = new FailoverInvoker();
                try {
                    invoker.invoke(() -> {
                        ServiceInstance si = loadBalancer.choose(serviceInstances);
                        try {
                            Connection connection = piccoloClient.getGatewayConnectionFactory().getConnection(si.getHostAndPort());
                            IdGenMessage idGenMessage = new IdGenMessage(connection);
                            idGenMessage.tag = isInit ? INIT_TAG : null;
                            idGenMessage.batchSize = BUFFER_SIZE;
                            connection.sendAsync(idGenMessage, future -> {
                                if (!future.isSuccess()) {
                                    LOGGER.warn("get xid async send failure, cause: {}", future.cause());
                                    sending.set(false);
                                }
                            });
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
}
