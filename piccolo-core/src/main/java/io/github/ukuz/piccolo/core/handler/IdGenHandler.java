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
import io.github.ukuz.piccolo.api.id.IdGenException;
import io.github.ukuz.piccolo.common.message.IdGenMessage;
import io.github.ukuz.piccolo.common.message.IdGenOkMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ukuz90
 */
public class IdGenHandler extends ChannelHandlerDelegateAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(IdGenHandler.class);

    public IdGenHandler(PiccoloContext piccoloContext, ChannelHandler handler) {
        super(piccoloContext, handler);
    }

    @Override
    public void received(Connection connection, Object message) throws ExchangeException {
        if (message instanceof IdGenMessage) {
            IdGenMessage msg = (IdGenMessage) message;

            int batchSize = Math.max(Math.min(msg.batchSize, 10000), 1);
            byte code = 0;
            long[] xid = new long[batchSize];
            int i = 0;
            for (; i < batchSize; i++) {
                try {
                    xid[i] = piccoloContext.getIdGen().get(msg.tag);
                } catch (IdGenException e) {
                    code = -1;
                    break;
                }
            }
            if (code != 0) {
                LOGGER.error("idGen failure, success: {} total: {}", i, batchSize);
                connection.sendAsync(IdGenOkMessage.build(connection).code(code).id(msg.id));
            } else {
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("idGen success, success: {} total: {}", i, batchSize);
                }
                connection.sendAsync(IdGenOkMessage.build(connection).xid(xid).tag(msg.tag).id(msg.id));
            }

        } else {
            super.received(connection, message);
        }
    }
}
