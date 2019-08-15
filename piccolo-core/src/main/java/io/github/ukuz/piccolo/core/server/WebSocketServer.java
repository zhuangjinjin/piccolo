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
package io.github.ukuz.piccolo.core.server;

import io.github.ukuz.piccolo.api.PiccoloContext;
import io.github.ukuz.piccolo.api.connection.ConnectionManager;
import io.github.ukuz.piccolo.api.exchange.handler.ChannelHandler;
import io.github.ukuz.piccolo.api.external.common.Assert;
import io.github.ukuz.piccolo.api.service.discovery.DefaultServiceInstance;
import io.github.ukuz.piccolo.api.service.discovery.ServiceInstance;
import io.github.ukuz.piccolo.api.service.registry.Registration;
import io.github.ukuz.piccolo.common.ServiceNames;
import io.github.ukuz.piccolo.common.properties.NetProperties;
import io.github.ukuz.piccolo.common.thread.ThreadNames;
import io.github.ukuz.piccolo.core.externel.handler.WebSocketIndexHandler;
import io.github.ukuz.piccolo.core.handler.WebSocketChannelHandler;
import io.github.ukuz.piccolo.core.properties.ThreadProperties;
import io.github.ukuz.piccolo.registry.zookeeper.ZKRegistration;
import io.github.ukuz.piccolo.transport.codec.Codec;
import io.github.ukuz.piccolo.transport.connection.NettyConnectionManager;
import io.github.ukuz.piccolo.transport.server.NettyServer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;

import java.net.InetSocketAddress;

/**
 * @author ukuz90
 */
public class WebSocketServer extends NettyServer {

    private InetSocketAddress address;
    private final String host;
    private final int port;
    private ZKRegistration serviceInstance;

    public WebSocketServer(PiccoloContext piccoloContext) {
        this(piccoloContext, new WebSocketChannelHandler(piccoloContext, null), new NettyConnectionManager());
    }

    public WebSocketServer(PiccoloContext piccoloContext, ChannelHandler channelHandler, ConnectionManager cxnxManager) {
        this(piccoloContext, channelHandler, cxnxManager,
                piccoloContext.getProperties(NetProperties.class).getWsServer().getBindIp(),
                piccoloContext.getProperties(NetProperties.class).getWsServer().getBindPort());
    }

    public WebSocketServer(PiccoloContext piccoloContext, ChannelHandler channelHandler, ConnectionManager cxnxManager, String host, int port) {
        super(piccoloContext, channelHandler, cxnxManager);
        Assert.notEmptyString(host, "host must not be empty");
        Assert.isTrue(port >= 0, "port was invalid port: " + port);
        this.host = host;
        this.port = port;
    }

    @Override
    protected Codec newCodec() {
        return null;
    }

    @Override
    protected void doInit() {
        address = new InetSocketAddress(host, port);

        ServiceInstance si = DefaultServiceInstance.builder()
                .host(host)
                .port(port)
                .isPersistent(false)
                .serviceId(ServiceNames.S_WS)
                .build();
        serviceInstance = new ZKRegistration(si);
    }

    @Override
    protected void initPipeline(ChannelPipeline pipeline) {
        //duplex
        pipeline.addLast(new HttpServerCodec());
        //inbound
        pipeline.addLast(new HttpObjectAggregator(65536));
        //duplex (webSocket handshake)
        pipeline.addLast(new WebSocketServerCompressionHandler());
        //inbound
        pipeline.addLast(new WebSocketServerProtocolHandler(piccoloContext.getProperties(NetProperties.class).getWsPath(), null, true));
        //inbound
        pipeline.addLast(new WebSocketIndexHandler());
        //duplex
        pipeline.addLast(getServerHandler());
    }

    @Override
    protected void doDestroy() {
        cxnxManager.destroy();
    }

    @Override
    protected InetSocketAddress getInetSocketAddress() {
        return address;
    }

    @Override
    public int getWorkerIORatio() {
        return 70;
    }

    @Override
    public int getBossThreadNum() {
        return 1;
    }

    @Override
    public int getWorkerThreadNum() {
        return piccoloContext.getProperties(ThreadProperties.class).getWsWorkerThreadNum();
    }

    @Override
    public String getBossThreadName() {
        return ThreadNames.T_WS_BOSS;
    }

    @Override
    public String getWorkerThreadName() {
        return ThreadNames.T_WS_WORKER;
    }

    @Override
    public String getId() {
        return null;
    }

    @Override
    public Registration getRegistration() {
        return serviceInstance;
    }
}
