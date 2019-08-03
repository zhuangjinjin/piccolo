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

import io.github.ukuz.piccolo.api.exchange.ExchangeException;
import io.github.ukuz.piccolo.api.exchange.support.MultiMessage;
import io.netty.channel.Channel;

/**
 * @author ukuz90
 */
public class MultiMessageHandler extends ChannelHandlerDelegateAdapter {

    public MultiMessageHandler(ChannelHandler handler) {
        super(handler);
    }

    @Override
    public void received(Channel channel, Object message) throws ExchangeException {
        if (message instanceof MultiMessage) {
            ((MultiMessage) message).forEach(msg -> handler.received(channel, msg));
        } else {
            handler.received(channel, message);
        }
    }


}