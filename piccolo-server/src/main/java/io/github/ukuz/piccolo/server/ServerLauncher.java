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

import java.io.*;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.util.HashMap;
import java.util.Map;

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
            try {
                InputStream is = ServerLauncher.class.getClassLoader().getResourceAsStream("banner.txt");
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                String line;
                while ((line = br.readLine()) != null) {
                    System.out.println(line);
                }
            } catch (Exception e) {
                e.printStackTrace();
                logger.info("server launch success!!!");
            }
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

        processChain.addLast(new ServiceRegistryBoot(server.getServiceRegistry()));
        processChain.addLast(new MQClientBoot(server.getMQClient(), server));
        processChain.addLast(new CacheManagerBoot(server.getCacheManager(), server));
        processChain.addLast(new ServerBoot(server.getGatewayServer(), true));
        processChain.addLast(new ServerBoot(server.getConnectServer()));
        processChain.addLast(new ServerBoot(server.getWebSocketServer()));
        processChain.addLast(new RouterCenterBoot(server.getRouterCenter()));
        processChain.addLast(new IdGenBoot(server.getIdGen()));
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
