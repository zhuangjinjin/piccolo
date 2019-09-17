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
import io.github.ukuz.piccolo.api.common.remote.FailoverInvoker;
import io.github.ukuz.piccolo.api.common.utils.StringUtils;
import io.github.ukuz.piccolo.api.connection.Connection;
import io.github.ukuz.piccolo.api.connection.SessionContext;
import io.github.ukuz.piccolo.api.exchange.ExchangeException;
import io.github.ukuz.piccolo.api.exchange.handler.ChannelHandler;
import io.github.ukuz.piccolo.api.external.common.Assert;
import io.github.ukuz.piccolo.api.mq.MQClient;
import io.github.ukuz.piccolo.api.route.RouteLocator;
import io.github.ukuz.piccolo.api.spi.SpiLoader;
import io.github.ukuz.piccolo.common.message.DispatcherMessage;
import io.github.ukuz.piccolo.common.message.ErrorMessage;
import io.github.ukuz.piccolo.common.message.push.KafkaDispatcherMqMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.github.ukuz.piccolo.mq.kafka.Topics.*;

/**
 * @author ukuz90
 */
public class DispatcherHandler implements ChannelHandler {

    private final static Logger LOGGER = LoggerFactory.getLogger(DispatcherHandler.class);

    private final ChannelHandler channelHandler;
    private final PiccoloContext piccoloContext;
    private static RouteLocator routeLocator;

    public DispatcherHandler(PiccoloContext piccoloContext, ChannelHandler channelHandler) {
        Assert.notNull(piccoloContext, "piccoloContext must not be null");
        this.channelHandler = channelHandler;
        this.piccoloContext = piccoloContext;
        routeLocator = SpiLoader.getLoader(RouteLocator.class).getExtension();
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

    @SuppressWarnings("unchecked")
    @Override
    public void received(Connection connection, Object message) throws ExchangeException {
        if (message instanceof DispatcherMessage) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("DispatcherHandler received message: {}", message);
            }

            DispatcherMessage msg = (DispatcherMessage) message;
            //判断是否绑定用户了
            SessionContext context = connection.getSessionContext();
            if (context.getUserId() == null) {
                connection.sendAsyncAndClose(ErrorMessage.build(msg).reason("not bind user"));
                LOGGER.error("dispatcher failure, cause: not bind user");
            }
            FailoverInvoker invoker = new FailoverInvoker();
            try {
                invoker.invoke(() -> {
                    MQClient client = piccoloContext.getMQClient();
                    long xid = piccoloContext.getIdGen().get("dispatch");
                    String uid = connection.getSessionContext().getUserId();
                    String topic = (String) routeLocator.getRoute(msg.routeKey);

                    KafkaDispatcherMqMessage mqMessage = new KafkaDispatcherMqMessage();
                    mqMessage.setXid(xid);
                    mqMessage.setMqClient(client);
                    mqMessage.setPayload(msg.payload);
                    mqMessage.setUid(uid);

                    if (StringUtils.hasText(topic)) {
                        client.publish(topic, uid, mqMessage.encode());
                    } else {
                        //识别不到的路由
                        client.publish(DISPATCH_MESSAGE.getTopic(), uid, mqMessage.encode());
                    }

                    return null;
                });
            } catch (Exception e) {
                e.printStackTrace();
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
