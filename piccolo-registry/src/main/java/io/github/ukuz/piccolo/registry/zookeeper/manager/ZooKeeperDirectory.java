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
package io.github.ukuz.piccolo.registry.zookeeper.manager;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.transaction.CuratorOp;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author ukuz90
 */
public class ZooKeeperDirectory implements ConnectionStateListener {

    private final Logger logger = LoggerFactory.getLogger(ZooKeeperDirectory.class);

    private CuratorFramework client;
    private Map<String, String> ephemeralNodes = new LinkedHashMap<>(4);
    private Map<String, String> ephemeralSequentialNodes = new LinkedHashMap<>(1);
    private Map<String, String> persistSequentialNodes = new LinkedHashMap<>(4);
    private final String watchPath;
    private TreeCache localCache;

    public ZooKeeperDirectory(CuratorFramework client, String watchPath) {
        this.client = client;
        this.watchPath = watchPath;
    }

    void start() throws Exception {
        localCache = new TreeCache(client, watchPath);
        localCache.start();
        client.getConnectionStateListenable().addListener(this);
    }

    public void reRegisterEphemeralNode(String path, String val) {
        registerEphemeralNode(path, val, false);
    }

    public void registerEphemeralNode(String path, String val) {
        registerEphemeralNode(path, val, true);
    }

    void registerEphemeralNode(String path, String val, boolean cahceNode) {
        try {
            path = getFullPath(path);
            if (existed(path)) {
                client.delete().deletingChildrenIfNeeded().forPath(path);
            }
            client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(path, val.getBytes(StandardCharsets.UTF_8));

            if (cahceNode) {
                ephemeralNodes.put(path, val);
            }
        } catch (Exception e) {
            logger.warn("registerEphemeralNode failure, path: {} val: {} cacheNode: {} cause: {}", path, val, cahceNode, e);
        }
    }

    public void reRegisterEphemeralSequentialNode(String path, String val) {
        registerEphemeralSequentialNode(path, val, false);
    }

    public void registerEphemeralSequentialNode(String path, String val) {
        registerEphemeralSequentialNode(path, val, true);
    }

    void registerEphemeralSequentialNode(String path, String val, boolean cacheNode) {
        try {
            path = getFullPath(path);
            client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL_SEQUENTIAL).forPath(path, val.getBytes(StandardCharsets.UTF_8));

            if (cacheNode) {
                ephemeralSequentialNodes.put(path, val);
            }
        } catch (Exception e) {
            logger.warn("registerEphemeralSequentialNode failure, path: {} val: {} cacheNode: {} cause: {}", path, val, cacheNode, e);
        }
    }

    public boolean existed(String path) throws Exception {
        path = getFullPath(path);
        Stat stat = client.checkExists().forPath(path);
        return stat != null;
    }

    @Override
    public void stateChanged(CuratorFramework client, ConnectionState newState) {
        logger.info("zk stateChanged newState: {} isConnected: {}", newState, newState.isConnected());
        if (newState == ConnectionState.RECONNECTED) {
            ephemeralNodes.forEach(this::reRegisterEphemeralNode);
            ephemeralSequentialNodes.forEach(this::reRegisterEphemeralSequentialNode);
        }
    }

    public void registerListener(TreeCacheListener listener) {
        localCache.getListenable().addListener(listener);
    }

    public void unregisterListenr(TreeCacheListener listener) {
        localCache.getListenable().removeListener(listener);
    }

    public void stop() {
        if (localCache != null) {
            localCache.close();
        }
    }

    public List<String> getChildrenKeys(String path) {
        try {
            path = getFullPath(path);
            if (!existed(path)) {
                return Collections.emptyList();
            }
            List<String> result = client.getChildren().forPath(path);
            result.sort(Comparator.reverseOrder());
            return result;
        } catch (Exception e) {
            logger.error("getChildrenKeys failure, cause: {}", e);
            return Collections.emptyList();
        }
    }
    
    public String getData(String path) {
        if (localCache == null) {
            return null;
        }
        path = getFullPath(path);
        ChildData data = localCache.getCurrentData(path);
        if (data != null) {
            return data.getData() == null ? null : new String(data.getData(), StandardCharsets.UTF_8);
        }
        return getDataFromRemote(path);
    }

    private String getDataFromRemote(String path) {
        try {
            path = getFullPath(path);
            if (existed(path)) {
                return new String(client.getData().forPath(path), StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            logger.error("getDataFromRemote failure, path: {} cause: {}", path, e);
        }
        return null;
    }



    public void updateData(String path, String data) {
        try {
//            client.inTransaction()
//                    .check().forPath(path)
//                    .and()
//                    .setData().forPath(path, data.getBytes(StandardCharsets.UTF_8))
//                    .and().commit();
            path = getFullPath(path);
            CuratorOp check = client.transactionOp().check().forPath(path);
            CuratorOp setData = client.transactionOp().setData().forPath(path, data.getBytes(StandardCharsets.UTF_8));
            client.transaction().forOperations(check, setData);

        } catch (Exception e) {

        }
    }

    public void removePath(String path) throws ZooKeeperException {
        try {
            path = getFullPath(path);
            client.delete().deletingChildrenIfNeeded().forPath(path);
        } catch (Exception e) {
            logger.error("removePath failure, path: {} cause: {}", path, e);
            throw new ZooKeeperException("removePath failure path: " + path, e);
        }
    }

    public void registerPersistNode(String path, String data) throws ZooKeeperException {
        try {
            path = getFullPath(path);
            if (existed(path)) {
                updateData(path, data);
            } else {
                client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path, data.getBytes(StandardCharsets.UTF_8));
            }
        } catch (Exception e) {
            logger.error("registerPersist failure, path: {} data: {} cause: {}", path, data, e);
            throw new ZooKeeperException("registerPersist failure path: " + path + " data: " + data, e);
        }
    }

    public String registerPersistSequentialNode(String path, String data) {
        return registerPersistSequentialNode(path, data, true);
    }

    String registerPersistSequentialNode(String path, String data, boolean cacheNode) {
        try {
            path = getFullPath(path);
            String newPath = client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT_SEQUENTIAL).forPath(path, data.getBytes(StandardCharsets.UTF_8));

            if (cacheNode) {
                persistSequentialNodes.put(path, data);
            }
            return newPath;
        } catch (Exception e) {
            logger.warn("registerEphemeralSequentialNode failure, path: {} val: {} cacheNode: {} cause: {}", path, data, cacheNode, e);
        }
        return path;
    }

    public String getFullPath(String path) {
        if (path.indexOf(watchPath) != 0) {
            path = path.charAt(0) == '/' ? watchPath + path : watchPath + "/" + path;
        }
        return path;
    }

}
