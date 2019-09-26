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
import io.github.ukuz.piccolo.api.common.utils.StringUtils;
import io.github.ukuz.piccolo.api.connection.ConnectionManager;
import io.github.ukuz.piccolo.api.exchange.handler.ChannelHandler;
import io.github.ukuz.piccolo.api.exchange.support.PacketToMessageConverter;
import io.github.ukuz.piccolo.api.external.common.Assert;
import io.github.ukuz.piccolo.api.service.discovery.DefaultServiceInstance;
import io.github.ukuz.piccolo.api.service.discovery.ServiceInstance;
import io.github.ukuz.piccolo.api.service.registry.Registration;
import io.github.ukuz.piccolo.api.spi.SpiLoader;
import io.github.ukuz.piccolo.common.ServiceNames;
import io.github.ukuz.piccolo.common.properties.NetProperties;
import io.github.ukuz.piccolo.common.thread.ThreadNames;
import io.github.ukuz.piccolo.core.externel.handler.WebSocketIndexHandler;
import io.github.ukuz.piccolo.core.handler.ChannelHandlers;
import io.github.ukuz.piccolo.core.properties.ThreadProperties;
import io.github.ukuz.piccolo.transport.codec.*;
import io.github.ukuz.piccolo.transport.connection.NettyConnectionManager;
import io.github.ukuz.piccolo.transport.server.NettyServer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.ServerSocketChannel;
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
    private DefaultServiceInstance serviceInstance;

    public WebSocketServer(PiccoloContext piccoloContext) {
        this(piccoloContext, ChannelHandlers.newConnectChannelHandler(piccoloContext), new NettyConnectionManager());
    }

    public WebSocketServer(PiccoloContext piccoloContext, ChannelHandler channelHandler, ConnectionManager cxnxManager) {
        this(piccoloContext, channelHandler, cxnxManager,
                piccoloContext.getProperties(NetProperties.class).getWsServer().getBindIp(),
                piccoloContext.getProperties(NetProperties.class).getWsServer().getBindPort());
    }

    public WebSocketServer(PiccoloContext piccoloContext, ChannelHandler channelHandler, ConnectionManager cxnxManager, String host, int port) {
        super(piccoloContext, channelHandler, cxnxManager);
        Assert.isTrue(port >= 0, "port was invalid port: " + port);
        address = StringUtils.hasText(host) ? new InetSocketAddress(host, port) : new InetSocketAddress(port);
    }

    @Override
    protected Codec newCodec() {
        return new MultiPacketCodec(SpiLoader.getLoader(PacketToMessageConverter.class).getExtension());
    }

    @Override
    protected void doInit() {
    }

    @Override
    protected void doStartComplete(ServerSocketChannel channel) {
        serviceInstance = DefaultServiceInstance.build()
                .host(piccoloContext.getProperties(NetProperties.class).getPublicIp())
//                .host(channel.localAddress().getAddress().getHostAddress())
                .port(channel.localAddress().getPort())
                .isPersistent(false)
                .serviceId(ServiceNames.S_WS);
    }

    @Override
    protected void initPipeline(ChannelPipeline pipeline) {
        BinaryFrameDuplexCodec codec = new BinaryFrameDuplexCodec(cxnxManager, newCodec());
        //duplex
        pipeline.addLast(new HttpServerCodec());
        //inbound
        pipeline.addLast(new HttpObjectAggregator(65536));
        //duplex
        pipeline.addLast(new WebSocketServerCompressionHandler());
        //inbound (webSocket handshake)
        //Sec-webSocket-version:7 use WebSocketServerHandshaker07
        //Sec-webSocket-version:8 use WebSocketServerHandshaker08
        //Sec-webSocket-version:13 use WebSocketServerHandshaker13
        //header not specified use WebSocketServerHandshaker00
        pipeline.addLast(new WebSocketServerProtocolHandler(piccoloContext.getProperties(NetProperties.class).getWsPath(), null, true));
        //inbound
        pipeline.addLast(new WebSocketIndexHandler());
        pipeline.addLast(codec.getDecoder());
        pipeline.addLast(codec.getEncoder());
        //duplex
        pipeline.addLast(getServerHandler());
    }

    @Override
    protected void initOptions(ServerBootstrap server) {
        super.initOptions(server);
        server.option(ChannelOption.SO_BACKLOG, 10240);

        NetProperties net = piccoloContext.getProperties(NetProperties.class);

        server.childOption(ChannelOption.SO_SNDBUF, net.getWsServer().getSndBuf() > 0 ? net.getWsServer().getSndBuf() : 32 * 1024);
        server.childOption(ChannelOption.SO_RCVBUF, net.getWsServer().getRcvBuf() > 0 ? net.getWsServer().getRcvBuf() : 32 * 1024);
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

    @Override
    public boolean isSecurity() {
        return false;
    }
}
