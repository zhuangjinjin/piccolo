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
package io.github.ukuz.piccolo.client.id.snowflake;

import io.github.ukuz.piccolo.api.connection.Connection;
import io.github.ukuz.piccolo.api.exchange.ExchangeException;
import io.github.ukuz.piccolo.client.PiccoloClient;
import io.github.ukuz.piccolo.client.common.ClientChannelHandlerAdaptor;
import io.github.ukuz.piccolo.client.id.IdGenManager;
import io.github.ukuz.piccolo.common.message.IdGenOkMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ukuz90
 */
public class IdGenClientHandler extends ClientChannelHandlerAdaptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(IdGenClientHandler.class);
    private final PiccoloClient context;

    public IdGenClientHandler(PiccoloClient context) {
        this.context = context;
    }

    @Override
    public void received(Connection connection, Object message) throws ExchangeException {
        if (message instanceof IdGenOkMessage) {

            IdGenOkMessage msg = (IdGenOkMessage) message;
            SnowflakeIdGenDelegate idGen = (SnowflakeIdGenDelegate) IdGenManager.getInstance().get(msg.getId());

            if (SnowflakeIdGenDelegate.INIT_TAG.equals(msg.getTag())) {
                idGen.writeData(msg.getXid(), true);
            } else {
                idGen.writeData(msg.getXid(), false);
            }

            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("received idGen message, msg: {}", msg);
            }
        } else {
            super.received(connection, message);
        }
    }
}
