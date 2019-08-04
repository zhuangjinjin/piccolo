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

import io.github.ukuz.piccolo.api.exchange.handler.ChannelHandler;
import io.github.ukuz.piccolo.transport.codec.DuplexCodec;
import io.github.ukuz.piccolo.transport.codec.MultiPacketCodec;
import io.github.ukuz.piccolo.transport.eventloop.EventLoopGroupFactory;
import io.github.ukuz.piccolo.transport.server.NettyServer;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelOutboundHandler;

import java.net.InetSocketAddress;

/**
 * @author ukuz90
 */
public class GatewayServer extends NettyServer {

    private DuplexCodec codec;
    private InetSocketAddress address;
    private final String host;
    private final int port;

    public GatewayServer(EventLoopGroupFactory eventLoopGroupFactory, ChannelHandler channelHandler,
                         String host, int port) {
        super(eventLoopGroupFactory, channelHandler);
        this.host = host;
        this.port = port;
    }

    @Override
    protected ChannelOutboundHandler getEncoder() {
        return codec.getEncoder();
    }

    @Override
    protected ChannelInboundHandler getDecoder() {
        return codec.getDecoder();
    }

    @Override
    protected void doInit() {
        codec = new DuplexCodec(new MultiPacketCodec());
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
