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
import io.github.ukuz.piccolo.registry.zookeeper.manager.ZooKeeperManager;
import io.github.ukuz.piccolo.registry.zookeeper.properties.ZooKeeperProperties;
import org.apache.curator.utils.ZKPaths;

/**
 * @author ukuz90
 */
public class ZooKeeperWorkerIdHolder implements WorkerIdHolder {

    private final static String WATCH_PATH = ZKPaths.PATH_SEPARATOR + "snowflake";

    private ZooKeeperManager zkManager;
    private long workerId;

    public ZooKeeperWorkerIdHolder(PiccoloContext context) {
        zkManager = new ZooKeeperManager(context.getProperties(ZooKeeperProperties.class), WATCH_PATH);
    }

    @Override
    public void init() {
        zkManager.start();
        //TODO 暂时
        workerId = 1;
    }

    @Override
    public long getWorkerId() {
        return workerId;
    }

}
