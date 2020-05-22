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
package io.github.ukuz.piccolo.client.websocket;

import io.github.ukuz.piccolo.api.connection.Connection;
import io.github.ukuz.piccolo.api.exchange.support.PacketToMessageConverter;
import io.github.ukuz.piccolo.api.spi.SpiLoader;
import io.github.ukuz.piccolo.common.message.BindUserMessage;
import io.github.ukuz.piccolo.common.message.HandshakeMessage;
import io.github.ukuz.piccolo.transport.codec.BinaryFrameDuplexCodec;
import io.github.ukuz.piccolo.transport.codec.Codec;
import io.github.ukuz.piccolo.transport.codec.MultiPacketCodec;
import io.github.ukuz.piccolo.transport.connection.NettyConnectionManager;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketClientCompressionHandler;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author ukuz90
 */
public class WebSocketClient  {

    private Bootstrap client;
    private EventLoopGroup group;
    private WebSocketClientHandler handler;
    private NettyConnectionManager cxnxManager;

    public WebSocketClient(URI uri) {
        client = new Bootstrap();
        group = new NioEventLoopGroup();
        cxnxManager = new NettyConnectionManager();
        handler = new WebSocketClientHandler(
                WebSocketClientHandshakerFactory.newHandshaker(uri,
                        WebSocketVersion.V13,
                        null,
                        true,
                        new DefaultHttpHeaders()), cxnxManager);
        client.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        Codec c = new MultiPacketCodec(SpiLoader.getLoader(PacketToMessageConverter.class).getExtension("client"));
                        BinaryFrameDuplexCodec codec = new BinaryFrameDuplexCodec(cxnxManager, c);
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new HttpClientCodec());
                        pipeline.addLast(new HttpObjectAggregator(65536));
                        pipeline.addLast(WebSocketClientCompressionHandler.INSTANCE);
                        pipeline.addLast(codec.getDecoder());
                        pipeline.addLast(codec.getEncoder());
                        pipeline.addLast(handler);
                    }
                });

    }

    public void setClientHandler(WebSocketClientHandler.BaseHandler handler) {
        this.handler.setHandler(handler);
    }

    public Connection start(URI uri) throws InterruptedException {
        Channel channel = client.connect(uri.getHost(), uri.getPort()).sync().channel();
        handler.handshakeFuture().sync();
        Connection connection = cxnxManager.getConnection(channel);
        handshake(connection);
        return connection;
    }

    private void handshake(Connection connection) {
        HandshakeMessage msg = new HandshakeMessage(connection);
        msg.deviceId = "xd32323";
        msg.osName = "web";
        msg.osVersion = "10";
        msg.clientVersion = "1.0.0";
        msg.timestamp = System.currentTimeMillis();
        msg.minHeartbeat = 5;
        msg.maxHeartbeat = 10;
        connection.sendAsync(msg);
    }

    public void bindUser(Connection connection, String userId) {
        BindUserMessage msg = new BindUserMessage(connection);
        msg.userId = userId;
        msg.tags = "";
        connection.sendAsync(msg);
    }

    public static void main(String[] args) throws URISyntaxException, InterruptedException {
        URI uri = new URI("ws://127.0.0.1:8089/piccolo");
        WebSocketClient client = new WebSocketClient(uri);
        client.start(uri);
    }

}
