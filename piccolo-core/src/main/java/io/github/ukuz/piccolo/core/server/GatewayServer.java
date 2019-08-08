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
import io.github.ukuz.piccolo.api.exchange.support.PacketToMessageConverter;
import io.github.ukuz.piccolo.api.spi.SpiLoader;
import io.github.ukuz.piccolo.core.handler.ChannelHandlers;
import io.github.ukuz.piccolo.transport.codec.Codec;
import io.github.ukuz.piccolo.transport.codec.MultiPacketCodec;
import io.github.ukuz.piccolo.transport.connection.NettyConnectionManager;
import io.github.ukuz.piccolo.transport.eventloop.EventLoopGroupFactory;
import io.github.ukuz.piccolo.transport.server.NettyServer;
import io.netty.channel.ChannelFactory;

import java.net.InetSocketAddress;

/**
 * @author ukuz90
 */
public class GatewayServer extends NettyServer {

    private InetSocketAddress address;
    private final String host;
    private final int port;
    private final ConnectionManager cxnxManager;

    public GatewayServer(PiccoloContext piccoloContext, String host, int port) {
        this(piccoloContext, ChannelHandlers.newChannelHandler(piccoloContext,null), new NettyConnectionManager(), host, port);
    }

    public GatewayServer(PiccoloContext piccoloContext,
                         ChannelHandler channelHandler, ConnectionManager cxnxManager,
                         String host, int port) {
        super(piccoloContext, channelHandler, cxnxManager);
        this.cxnxManager = cxnxManager;
        this.host = host;
        this.port = port;
    }

    @Override
    protected Codec newCodec() {
        return new MultiPacketCodec(SpiLoader.getLoader(PacketToMessageConverter.class).getExtension());
    }

    @Override
    protected void doInit() {
        address = new InetSocketAddress(host, port);

    }

    @Override
    protected void doDestory() {

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
        return 4;
    }

    @Override
    public int getWorkerThreadNum() {
        return 4;
    }

    @Override
    public String getBossThreadName() {
        return "piccolo-gateway-boss-pool";
    }

    @Override
    public String getWorkerThreadName() {
        return "piccolo-gateway-worker-pool";
    }

    @Override
    public String getId() {
        return null;
    }
}
