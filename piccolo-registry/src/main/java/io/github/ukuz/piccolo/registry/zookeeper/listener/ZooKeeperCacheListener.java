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
package io.github.ukuz.piccolo.registry.zookeeper.listener;

import io.github.ukuz.piccolo.api.external.common.Assert;
import io.github.ukuz.piccolo.api.service.discovery.DefaultServiceInstance;
import io.github.ukuz.piccolo.api.service.discovery.ServiceListener;
import io.github.ukuz.piccolo.common.json.Jsons;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;

import java.util.Objects;

/**
 * @author ukuz90
 */
public class ZooKeeperCacheListener implements TreeCacheListener {

    private final String watchPath;
    private final ServiceListener listener;

    public ZooKeeperCacheListener(String watchPath, ServiceListener listener) {
        Assert.notNull(listener, "listener must not be null");
        Assert.notEmptyString(watchPath, "watchPath must not be empty");
        this.watchPath = watchPath;
        this.listener = listener;
    }

    @Override
    public void childEvent(CuratorFramework client, TreeCacheEvent event) throws Exception {
        ChildData data = event.getData();
        if (data == null) {
            return;
        }
        String path = data.getPath();
        if (path == null || path.isEmpty()) {
            return;
        }
        if (path.startsWith(watchPath)) {
            switch (event.getType()) {
                case NODE_ADDED:
                    listener.onServiceAdded(Jsons.fromJson(data.getData(), DefaultServiceInstance.class));
                    break;
                case NODE_REMOVED:
                    listener.onServiceDeleted(Jsons.fromJson(data.getData(), DefaultServiceInstance.class));
                    break;
                case NODE_UPDATED:
                    listener.onServiceUpdated(Jsons.fromJson(data.getData(), DefaultServiceInstance.class));
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {return true;}
        if (o == null || getClass() != o.getClass()) {return false;}
        ZooKeeperCacheListener that = (ZooKeeperCacheListener) o;
        return Objects.equals(watchPath, that.watchPath) &&
                Objects.equals(listener, that.listener);
    }

    @Override
    public int hashCode() {
        return Objects.hash(watchPath, listener);
    }
}
