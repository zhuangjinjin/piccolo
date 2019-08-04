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

import io.github.ukuz.piccolo.api.common.Assert;
import io.github.ukuz.piccolo.api.exchange.handler.ChannelHandler;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

/**
 * @author ukuz90
 */
public class ServerHandler extends ChannelDuplexHandler {

    private ChannelHandler handler;

    public ServerHandler(ChannelHandler handler) {
        Assert.notNull(handler, "handler must not be null");
        this.handler = handler;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        handler.connected(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        handler.disconnected(ctx.channel());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        handler.received(ctx.channel(), msg);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
        handler.sent(ctx.channel(), msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        handler.caught(ctx.channel(), cause);
    }
}
