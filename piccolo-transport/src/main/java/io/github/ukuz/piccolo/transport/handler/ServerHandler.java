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

import io.github.ukuz.piccolo.api.external.common.Assert;
import io.github.ukuz.piccolo.api.connection.Connection;
import io.github.ukuz.piccolo.api.connection.ConnectionManager;
import io.github.ukuz.piccolo.api.exchange.handler.ChannelHandler;
import io.github.ukuz.piccolo.transport.connection.NettyConnection;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ukuz90
 */
public class ServerHandler extends ChannelDuplexHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerHandler.class);
    private ConnectionManager cxnxManager;

    private ChannelHandler handler;

    public ServerHandler(ConnectionManager cxnxManager, ChannelHandler handler) {
        Assert.notNull(handler, "handler must not be null");
        Assert.notNull(cxnxManager, "cxnxManager must not be null");
        this.cxnxManager = cxnxManager;
        this.handler = handler;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        Connection connection = new NettyConnection();
        connection.init(ctx.channel(), false);
        cxnxManager.add(connection);
        LOGGER.info("handler active ctx: {} connection:{}", ctx, connection);
        handler.connected(connection);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        Connection connection = cxnxManager.removeConnection(ctx.channel());
        LOGGER.info("handler inactive ctx: {} connection:{}", ctx, connection);
        handler.disconnected(connection);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        Connection connection = cxnxManager.getConnection(ctx.channel());
        LOGGER.info("handler received ctx: {} connection:{} msg:{}", ctx, connection, msg);
        handler.received(connection, msg);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
        Connection connection = cxnxManager.getConnection(ctx.channel());
        LOGGER.info("handler write ctx: {} connection:{} msg:{}", ctx, connection, msg);
        handler.sent(connection, msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        Connection connection = cxnxManager.removeConnection(ctx.channel());
        LOGGER.error("handler occupy exception, ctx: {} connection:{} cause: {}", ctx, connection, cause.getMessage());
        handler.caught(connection, cause);
    }
}
