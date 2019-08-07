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

import io.github.ukuz.piccolo.api.exchange.handler.MultiMessageHandler;
import io.github.ukuz.piccolo.api.spi.SpiLoader;
import io.github.ukuz.piccolo.core.PiccoloServer;
import io.github.ukuz.piccolo.core.server.GatewayServer;
import io.github.ukuz.piccolo.server.boot.BootProcessChain;
import io.github.ukuz.piccolo.server.boot.DefaultBootProcessChain;
import io.github.ukuz.piccolo.server.boot.ServerBoot;
import io.github.ukuz.piccolo.transport.channel.ServerSocketChannelFactory;
import io.github.ukuz.piccolo.transport.eventloop.EventLoopGroupFactory;
import io.github.ukuz.piccolo.transport.handler.ServerHandler;
import io.netty.channel.EventLoopGroup;

/**
 * @author ukuz90
 */
public class ServerLauncher {

    private BootProcessChain processChain;
    private PiccoloServer server;

    public ServerLauncher() {

    }

    public void init() {
        if (server == null) {
            server = new PiccoloServer();
        }

        if (processChain == null) {
            processChain = newBootProcessChain();
        }

        EventLoopGroupFactory eventLoopGroupFactory = SpiLoader.getLoader(EventLoopGroupFactory.class).getExtension();
        ServerSocketChannelFactory channelFactory = SpiLoader.getLoader(ServerSocketChannelFactory.class).getExtension();
        String host = "localhost";
        int port = 8010;

        processChain.addLast(new ServerBoot(new GatewayServer(eventLoopGroupFactory, channelFactory
                , host, port)));
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
