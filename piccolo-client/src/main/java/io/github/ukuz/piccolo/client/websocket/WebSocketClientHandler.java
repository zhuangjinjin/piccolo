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

import io.github.ukuz.piccolo.api.config.Environment;
import io.github.ukuz.piccolo.api.exchange.support.MultiMessage;
import io.github.ukuz.piccolo.api.spi.SpiLoader;
import io.github.ukuz.piccolo.common.message.HandshakeOkMessage;
import io.github.ukuz.piccolo.transport.connection.NettyConnection;
import io.github.ukuz.piccolo.transport.connection.NettyConnectionManager;
import io.netty.channel.*;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.*;

import java.nio.charset.StandardCharsets;

/**
 * @author ukuz90
 */
public class WebSocketClientHandler extends SimpleChannelInboundHandler<Object> {

    private final WebSocketClientHandshaker handshaker;

    private final NettyConnectionManager cxnxManager;

    private ChannelPromise handshakeFuture;

    private BaseHandler handler;

    public WebSocketClientHandler(WebSocketClientHandshaker handshaker, NettyConnectionManager cxnxManager) {
        this.handshaker = handshaker;
        this.cxnxManager = cxnxManager;
    }

    public void setHandler(BaseHandler handler) {
        this.handler = handler;
    }

    public ChannelFuture handshakeFuture() {
        return handshakeFuture;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        handshakeFuture = ctx.newPromise();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        handshaker.handshake(ctx.channel());
        NettyConnection connection = new NettyConnection(SpiLoader.getLoader(Environment.class).getExtension());
        connection.init(ctx.channel(), false);
        cxnxManager.add(connection);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("WebSocket Client disconnected!");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        Channel channel = ctx.channel();
        if (!handshaker.isHandshakeComplete()) {
            try {
                handshaker.finishHandshake(channel, (FullHttpResponse) msg);
                System.out.println("WebSocket Client connected!");
                handshakeFuture.setSuccess();
            } catch (WebSocketHandshakeException e) {
                System.out.println("WebSocket Client failed to connect");
                handshakeFuture.setFailure(e);
            }
            return;
        }

        if (msg instanceof FullHttpResponse) {
            FullHttpResponse response = (FullHttpResponse) msg;
            throw new IllegalStateException(
                    "Unexpected FullHttpResponse (getStatus=" + response.status() +
                            ", content=" + response.content().toString(StandardCharsets.UTF_8) + ')');
        }

        MultiMessage multiMessage = (MultiMessage) msg;
        multiMessage.forEach(m -> {
            if (m instanceof HandshakeOkMessage) {
                System.out.println("WebSocket Client received message: " + m);
            }
            if (handler != null) {
                handler.handle(m);
            }
        });

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();

        if (!handshakeFuture.isDone()) {
            handshakeFuture.setFailure(cause);
        }

        ctx.close();
    }

    public interface BaseHandler {
        /**
         * 处理消息
         * @param msg
         */
        void handle(Object msg);

    }
}
