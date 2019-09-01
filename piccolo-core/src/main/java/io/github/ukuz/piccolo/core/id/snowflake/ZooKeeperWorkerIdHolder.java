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
package io.github.ukuz.piccolo.core.id.snowflake;

import io.github.ukuz.piccolo.api.PiccoloContext;
import io.github.ukuz.piccolo.api.common.threadpool.ExecutorFactory;
import io.github.ukuz.piccolo.api.external.common.Assert;
import io.github.ukuz.piccolo.api.service.discovery.ServiceInstance;
import io.github.ukuz.piccolo.common.json.Jsons;
import io.github.ukuz.piccolo.core.PiccoloServer;
import io.github.ukuz.piccolo.registry.zookeeper.manager.ZooKeeperManager;
import io.github.ukuz.piccolo.registry.zookeeper.properties.ZooKeeperProperties;
import lombok.Data;
import org.apache.curator.utils.ZKPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author ukuz90
 */
public class ZooKeeperWorkerIdHolder implements WorkerIdHolder {

    private final static Logger LOGGER = LoggerFactory.getLogger(ZooKeeperWorkerIdHolder.class);
    private final static String WATCH_PATH = ZKPaths.PATH_SEPARATOR + "snowflake";

    private ZooKeeperManager zkManager;
    private long workerId;
    private String host;
    private int port;
    private PiccoloContext context;
    private long lastUpdateTime;
    private String realPath;
    private ScheduledThreadPoolExecutor pool;

    public ZooKeeperWorkerIdHolder(PiccoloContext context) {
        this.context = context;
        zkManager = new ZooKeeperManager(context.getProperties(ZooKeeperProperties.class), WATCH_PATH);
    }

    @Override
    public void init() {
        zkManager.start();
        ServiceInstance si = ((PiccoloServer)context).getGatewayServer().getRegistration();
        host = si.getHost();
        port = si.getPort();
        try {
            List<String> children = zkManager.getDirectory().getChildrenKeys(WATCH_PATH);
            Map<String, String> map = children.stream()
                    .collect(Collectors.toMap(c -> c.split("-")[0], c -> c));
            if (!map.containsKey(si.getHostAndPort())) {
                //新加入的节点
                String path = WATCH_PATH + ZKPaths.PATH_SEPARATOR + si.getHostAndPort() + "-";
                realPath = zkManager.getDirectory().registerPersistSequentialNode(path, Jsons.toJson(build()));
                Assert.isTrue(realPath.split("-").length == 2, "init failure, newPath: " + realPath);
                workerId = Long.parseLong(realPath.split("-")[1]);
                scheduledUploadData();
            } else {
                realPath = WATCH_PATH + ZKPaths.PATH_SEPARATOR + map.get(si.getHostAndPort());
                Assert.isTrue(realPath.split("-").length == 2, "init failure, newPath: " + realPath);
                if (!checkInitTimeStamp()) {
                    LOGGER.error("init timestamp check error,forever node timestamp gt this node time");
                    throw new RuntimeException("init timestamp check error,forever node timestamp gt this node time");
                }
                workerId = Long.parseLong(realPath.split("-")[1]);
                scheduledUploadData();
            }

        } catch (Exception e) {
            LOGGER.error("worker id init failure, cause: {}", e);
        }
    }

    @Override
    public long getWorkerId() {
        return workerId;
    }

    @Override
    public void destroy() {
        uploadData();
        if (pool != null) {
            pool.shutdown();
        }
    }

    private boolean checkInitTimeStamp() {
        String data = zkManager.getDirectory().getData(realPath);
        EndPoint endPoint = Jsons.fromJson(data, EndPoint.class);
        //该节点的时间不能小于最后一次上报的时间
        return System.currentTimeMillis() >= endPoint.getTimestamp();
    }


    private void scheduledUploadData() {
        pool = (ScheduledThreadPoolExecutor) context.getExecutorFactory().create(ExecutorFactory.ID_GEN, context.getEnvironment());
        pool.scheduleWithFixedDelay(this::uploadData, 1, 3, TimeUnit.SECONDS);
    }

    private void uploadData() {
        if (System.currentTimeMillis() < lastUpdateTime) {
            return;
        }
        zkManager.getDirectory().updateData(realPath, Jsons.toJson(build()));
        lastUpdateTime = System.currentTimeMillis();
    }

    private EndPoint build() {
        EndPoint endPoint = new EndPoint();
        endPoint.setHost(host);
        endPoint.setPort(port);
        endPoint.setTimestamp(System.currentTimeMillis());
        return endPoint;
    }

    @Data
    static class EndPoint {
        private String host;
        private int port;
        private long timestamp;

    }

}
