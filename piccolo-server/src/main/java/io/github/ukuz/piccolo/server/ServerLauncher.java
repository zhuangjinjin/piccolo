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
package io.github.ukuz.piccolo.server;

import io.github.ukuz.piccolo.core.PiccoloServer;
import io.github.ukuz.piccolo.server.boot.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ukuz90
 */
public class ServerLauncher {

    private BootProcessChain processChain;
    private PiccoloServer server;
    private final Logger logger = LoggerFactory.getLogger(ServerLauncher.class);

    private BootJob lastJob = new BootJob() {
        @Override
        public void start() {
            logger.info("server launch success!!!");
        }
    };

    public ServerLauncher() {
    }

    public void init() {
        if (server == null) {
            server = new PiccoloServer();
        }

        if (processChain == null) {
            processChain = newBootProcessChain();
        }

        processChain.addLast(new ServiceRegistryBoot());
//        processChain.addLast(new ServiceDiscoveryBoot());

        processChain.addLast(new CacheManagerBoot(server.getCacheManager(), server));
        processChain.addLast(new RouterCenterBoot(server.getRouterCenter()));
        processChain.addLast(new ServerBoot(server.getGatewayServer()));
        processChain.addLast(new ServerBoot(server.getConnectServer()));
        processChain.addLast(lastJob);
    }

    void start() {
        processChain.start();
    }

    void stop() {
        processChain.stop();
    }

    private BootProcessChain newBootProcessChain() {
        return new DefaultBootProcessChain();
    }



}
