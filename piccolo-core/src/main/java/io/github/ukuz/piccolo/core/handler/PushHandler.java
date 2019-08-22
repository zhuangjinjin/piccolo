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
import io.github.ukuz.piccolo.api.common.utils.StringUtils;
import io.github.ukuz.piccolo.api.connection.Connection;
import io.github.ukuz.piccolo.api.exchange.ExchangeException;
import io.github.ukuz.piccolo.api.exchange.handler.ChannelHandler;
import io.github.ukuz.piccolo.common.message.DispatcherResponseMessage;
import io.github.ukuz.piccolo.common.message.PushMessage;
import io.github.ukuz.piccolo.core.PiccoloServer;
import io.github.ukuz.piccolo.core.router.LocalRouter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * @author ukuz90
 */
public class PushHandler implements ChannelHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(PushHandler.class);

    private final PiccoloContext piccoloContext;

    public PushHandler(PiccoloContext piccoloContext) {
        this.piccoloContext = piccoloContext;
    }

    @Override
    public void connected(Connection connection) throws ExchangeException {

    }

    @Override
    public void disconnected(Connection connection) throws ExchangeException {

    }

    @Override
    public void sent(Connection connection, Object message) throws ExchangeException {

    }

    @Override
    public void received(Connection connection, Object message) throws ExchangeException {
        if (message instanceof PushMessage) {
            PushMessage msg = (PushMessage) message;

            PiccoloServer piccoloServer = (PiccoloServer) piccoloContext;
            if (msg.broadcast) {
                Set<LocalRouter> localRouters = piccoloServer.getRouterCenter().lookupLocalAll();
                localRouters.forEach(localRouter -> {
                    Connection conn = localRouter.getRouterValue();
                    conn.sendAsync(DispatcherResponseMessage.build(connection).payload(msg.content));
                });
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("broadcast message, msg: {} conn's size: {}", msg, localRouters.size());
                }
            } else if (StringUtils.hasText(msg.userId)) {
                Set<LocalRouter> localRouters = piccoloServer.getRouterCenter().lookupLocal(msg.userId);
                if (localRouters != null && !localRouters.isEmpty()) {
                    localRouters.forEach(localRouter -> {
                        Connection conn = localRouter.getRouterValue();
                        conn.sendAsync(DispatcherResponseMessage.build(connection).payload(msg.content));
                    });
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("send a message, msg: {} conn's size: {}", msg, localRouters.size());
                    }
                } else {
                    LOGGER.warn("can not found alive connection, msg: {}", msg);
                }
            } else {
                LOGGER.error("received a invalid message, msg: {} conn: {}", message, connection);
            }

        } else {
            connection.close();
            LOGGER.error("handler unknown message, message: {} conn: {}", message, connection);
        }
    }

    @Override
    public void caught(Connection connection, Throwable exception) throws ExchangeException {

    }
}
