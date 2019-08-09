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
package io.github.ukuz.piccolo.client.gateway;

import io.github.ukuz.piccolo.api.config.Environment;
import io.github.ukuz.piccolo.api.connection.ConnectionManager;
import io.github.ukuz.piccolo.api.exchange.handler.ChannelHandler;
import io.github.ukuz.piccolo.api.external.common.Assert;
import io.github.ukuz.piccolo.api.service.ServiceException;
import io.github.ukuz.piccolo.client.ChannelHandlers;
import io.github.ukuz.piccolo.client.properties.ClientProperties;
import io.github.ukuz.piccolo.common.thread.ThreadNames;
import io.github.ukuz.piccolo.transport.client.NettyClient;
import io.github.ukuz.piccolo.transport.codec.Codec;
import io.github.ukuz.piccolo.transport.codec.MultiPacketCodec;
import io.github.ukuz.piccolo.transport.connection.NettyConnectionManager;

import java.net.InetSocketAddress;

/**
 * @author ukuz90
 */
public class GatewayClient extends NettyClient {

    private final String host;
    private final int port;
    private InetSocketAddress socketAddress;

    public GatewayClient(Environment environment) {
        this(environment,
                environment.getProperties(ClientProperties.class).getGatewayServerHost(),
                environment.getProperties(ClientProperties.class).getConnectServerPort());
    }

    public GatewayClient(Environment environment, String host, int port) {
        this(environment, new NettyConnectionManager(), ChannelHandlers.newGatewayClientHandler(environment), host, port);
    }

    public GatewayClient(Environment environment, ConnectionManager cxnxManager, ChannelHandler handler, String host, int port) {
        super(environment, cxnxManager, handler);
        Assert.notEmptyString(host, "host must not be empty");
        Assert.isTrue(port >= 0, "port was invalid port: " + port);
        this.host = host;
        this.port = port;
    }

    @Override
    public void init() throws ServiceException {
        socketAddress = new InetSocketAddress(host, port);
    }

    @Override
    protected InetSocketAddress getInetSocketAddress() {
        return socketAddress;
    }

    @Override
    protected String getWorkerThreadName() {
        return ThreadNames.T_TCP_CLIENT;
    }

    @Override
    protected Codec newCodec() {
        return new MultiPacketCodec();
    }
}
