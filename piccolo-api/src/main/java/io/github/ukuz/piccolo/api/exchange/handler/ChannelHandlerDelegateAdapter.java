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
package io.github.ukuz.piccolo.api.exchange.handler;

import io.github.ukuz.piccolo.api.common.Assert;
import io.github.ukuz.piccolo.api.exchange.ExchangeException;
import io.netty.channel.Channel;

/**
 * @author ukuz90
 */
public class ChannelHandlerDelegateAdapter implements ChannelHandlerDelegate {

    protected ChannelHandler handler;


    public ChannelHandlerDelegateAdapter(ChannelHandler handler) {
        Assert.notNull(handler, "Handler must not null");
        this.handler = handler;
    }

    @Override
    public void connected(Channel channel) throws ExchangeException {
        handler.connected(channel);
    }

    @Override
    public void disconnected(Channel channel) throws ExchangeException {
        handler.disconnected(channel);
    }

    @Override
    public void sent(Channel channel, Object message) throws ExchangeException {
        handler.sent(channel, message);
    }

    @Override
    public void received(Channel channel, Object message) throws ExchangeException {
        handler.received(channel, message);
    }

    @Override
    public void caught(Channel channel, Throwable exception) throws ExchangeException {
        handler.caught(channel, exception);
    }

    @Override
    public ChannelHandler getChannelHandler() {
        if (handler instanceof ChannelHandlerDelegate) {
            return ((ChannelHandlerDelegate) handler).getChannelHandler();
        }
        return handler;
    }
}
