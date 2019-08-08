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
package io.github.ukuz.piccolo.core.handler;

import io.github.ukuz.piccolo.api.PiccoloContext;
import io.github.ukuz.piccolo.api.connection.Connection;
import io.github.ukuz.piccolo.api.exchange.ExchangeException;
import io.github.ukuz.piccolo.api.exchange.handler.ChannelHandler;
import io.github.ukuz.piccolo.api.exchange.handler.ChannelHandlerDelegateAdapter;
import io.github.ukuz.piccolo.common.message.HeartbeatMessage;

/**
 * @author ukuz90
 */
public class HeartbeatHandler extends ChannelHandlerDelegateAdapter {


    public HeartbeatHandler(PiccoloContext piccoloContext, ChannelHandler handler) {
        super(piccoloContext, handler);
    }

    @Override
    public void received(Connection connection, Object message) throws ExchangeException {
        if (message instanceof HeartbeatMessage) {
            connection.sendAsync((HeartbeatMessage)message);
//            connection.sendAsync(new HeartbeatMessage(connection));
        }
        super.received(connection, message);
    }
}
