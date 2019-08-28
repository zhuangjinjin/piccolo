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
package io.github.ukuz.piccolo.transport.handler;

import io.github.ukuz.piccolo.api.PiccoloContext;
import io.github.ukuz.piccolo.api.config.Environment;
import io.github.ukuz.piccolo.api.connection.Connection;
import io.github.ukuz.piccolo.api.event.ConnectionConnectEvent;
import io.github.ukuz.piccolo.api.exchange.handler.ChannelHandler;
import io.github.ukuz.piccolo.api.external.common.Assert;
import io.github.ukuz.piccolo.api.connection.ConnectionManager;
import io.github.ukuz.piccolo.common.event.EventBus;
import io.github.ukuz.piccolo.transport.connection.NettyConnection;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ukuz90
 */
public class ClientHandler extends ChannelDuplexHandler {

    private final Logger logger = LoggerFactory.getLogger(ClientHandler.class);
    private ConnectionManager cxnxManager;

    private ChannelHandler handler;
    private Environment environment;

    public ClientHandler(Environment environment, ConnectionManager cxnxManager, ChannelHandler handler) {
        Assert.notNull(handler, "handler must not be null");
        Assert.notNull(cxnxManager, "cxnxManager must not be null");
        Assert.notNull(environment, "environment must not be null");
        this.environment = environment;
        this.cxnxManager = cxnxManager;
        this.handler = handler;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        NettyConnection connection = new NettyConnection(environment);
        connection.init(ctx.channel(), false);
        cxnxManager.add(connection);
        handler.connected(connection);
        EventBus.post(new ConnectionConnectEvent(connection));
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Connection connection = cxnxManager.removeConnection(ctx.channel());
        handler.disconnected(connection);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Connection connection = cxnxManager.getConnection(ctx.channel());
        handler.received(connection, msg);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        Connection connection = cxnxManager.getConnection(ctx.channel());
        handler.sent(connection, msg);
        ctx.write(msg, promise);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        Connection connection = cxnxManager.removeConnection(ctx.channel());
        handler.caught(connection, cause);
    }
}
