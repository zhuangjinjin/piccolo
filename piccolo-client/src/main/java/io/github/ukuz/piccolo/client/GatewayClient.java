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

import io.github.ukuz.piccolo.api.exchange.support.PacketToMessageConverter;
import io.github.ukuz.piccolo.api.service.ServiceException;
import io.github.ukuz.piccolo.api.spi.SpiLoader;
import io.github.ukuz.piccolo.transport.client.NettyClient;
import io.github.ukuz.piccolo.transport.codec.DuplexCodec;
import io.github.ukuz.piccolo.transport.codec.MultiPacketCodec;
import io.github.ukuz.piccolo.transport.eventloop.EventLoopGroupFactory;
import io.github.ukuz.piccolo.transport.handler.ClientHandler;
import io.netty.channel.ChannelFactory;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelOutboundHandler;

import java.net.InetSocketAddress;

/**
 * @author ukuz90
 */
public class GatewayClient extends NettyClient {

    private final String host;
    private final int port;

    private DuplexCodec codec;

    private InetSocketAddress socketAddress;

    public GatewayClient(EventLoopGroupFactory eventLoopGroupFactory, ChannelFactory channelFactory, ClientHandler handler, String host, int port) {
        super(eventLoopGroupFactory, channelFactory, handler);

        this.host = host;
        this.port = port;
    }

    @Override
    public void init() throws ServiceException {
        socketAddress = new InetSocketAddress(host, port);
        codec = new DuplexCodec(new MultiPacketCodec(SpiLoader.getLoader(PacketToMessageConverter.class).getExtension()));
    }

    @Override
    protected InetSocketAddress getInetSocketAddress() {
        return socketAddress;
    }

    @Override
    protected String getWorkerThreadName() {
        return "piccolo-gateway-client-pool";
    }

    @Override
    protected ChannelOutboundHandler getEncoder() {
        return codec.getEncoder();
    }

    @Override
    protected ChannelInboundHandler getDecoder() {
        return codec.getDecoder();
    }
}
