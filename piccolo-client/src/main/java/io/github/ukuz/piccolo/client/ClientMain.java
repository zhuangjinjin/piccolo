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
package io.github.ukuz.piccolo.client;

import io.github.ukuz.piccolo.api.spi.SpiLoader;
import io.github.ukuz.piccolo.transport.channel.SocketChannelFactory;
import io.github.ukuz.piccolo.transport.eventloop.EventLoopGroupFactory;
import io.github.ukuz.piccolo.transport.handler.ClientHandler;

/**
 * @author ukuz90
 */
public class ClientMain {

    public static void main(String[] args) {
        EventLoopGroupFactory eventLoopGroupFactory = SpiLoader.getLoader(EventLoopGroupFactory.class).getExtension();
        SocketChannelFactory channelFactory = SpiLoader.getLoader(SocketChannelFactory.class).getExtension();
        String host = "localhost";
        int port = 8010;
        GatewayClient client = new GatewayClient(eventLoopGroupFactory, channelFactory, new ClientHandler(), host, port);
        client.start();
    }

}
