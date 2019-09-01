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
package io.github.ukuz.piccolo.core.router;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import io.github.ukuz.piccolo.api.common.remote.FailoverInvoker;
import io.github.ukuz.piccolo.api.common.utils.StringUtils;
import io.github.ukuz.piccolo.api.connection.Connection;
import io.github.ukuz.piccolo.api.connection.SessionContext;
import io.github.ukuz.piccolo.api.event.RouterChangeEvent;
import io.github.ukuz.piccolo.api.id.IdGenException;
import io.github.ukuz.piccolo.api.mq.MQClient;
import io.github.ukuz.piccolo.api.mq.MQMessageReceiver;
import io.github.ukuz.piccolo.api.mq.MQTopic;
import io.github.ukuz.piccolo.api.router.ClientLocator;
import io.github.ukuz.piccolo.api.router.Router;
import io.github.ukuz.piccolo.common.event.EventObservable;
import io.github.ukuz.piccolo.common.json.Jsons;
import io.github.ukuz.piccolo.common.message.KickUserMessage;
import io.github.ukuz.piccolo.common.router.KafkaKickMqMessage;
import io.github.ukuz.piccolo.common.router.KickMqMessage;
import io.github.ukuz.piccolo.common.router.RemoteRouter;
import io.github.ukuz.piccolo.core.PiccoloServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;


/**
 * @author ukuz90
 */
public class RouterChangeListener extends EventObservable implements MQMessageReceiver {

    private static final Logger LOGGER = LoggerFactory.getLogger(RouterChangeListener.class);

    private PiccoloServer piccoloServer;
    private MQClient mqClient;
    private MQTopic kickUserTopic;

    public RouterChangeListener(PiccoloServer piccoloServer) {
        this.piccoloServer = piccoloServer;
        this.mqClient = piccoloServer.getMQClient();
    }

    public void init() {
        LOGGER.info("subscribe topic: {}", kickUserTopic);
        //订阅踢人
        String topic = getKickUserTopic(piccoloServer.getGatewayServer().getRegistration().getHostAndPort());
        this.kickUserTopic = new MQTopic(topic);
        mqClient.addTopicIfNeeded(kickUserTopic);
        mqClient.subscribe(kickUserTopic.getTopic(), this);
    }

    @Subscribe
    @AllowConcurrentEvents
    public void on(RouterChangeEvent event) {
        if (event.getRouter().getRouterType() == Router.RouterType.LOCAL) {
            sendKickUserMessageToClient(event.getUserId(), (LocalRouter) event.getRouter());
        } else {
            FailoverInvoker invoker = new FailoverInvoker();
            try {
                invoker.invoke(() -> {
                    sendKickUserMessageToMQClient(event.getUserId(), (RemoteRouter) event.getRouter());
                    return null;
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void sendKickUserMessageToClient(String userId, LocalRouter localRouter) {
        Connection connection = localRouter.getRouterValue();
        SessionContext context = connection.getSessionContext();
        KickUserMessage msg = KickUserMessage.build(connection);
        msg.userId(userId).deviceId(context.getDeviceId());

        connection.sendAsync(msg, future -> {
            if (future.isSuccess()) {
                LOGGER.info("kick user success, userId: {}", userId);
            } else {
                LOGGER.info("kick user failure, userId: {} cause: {}", userId, future.cause());
            }
        });
    }

    private void sendKickUserMessageToMQClient(String userId, RemoteRouter remoteRouter) throws IdGenException {
        long xid = piccoloServer.getIdGen().get("kick");
        KafkaKickMqMessage msg = new KafkaKickMqMessage();
        msg.setXid(xid);
        msg.setMqClient(piccoloServer.getMQClient());
        ClientLocator locator = remoteRouter.getRouterValue();
        msg.setConnId(locator.getConnId());
        msg.setClientType(locator.getClientType());
        msg.setDeviceId(locator.getDeviceId());
        msg.setTargetAddress(locator.getHost());
        msg.setTargetPort(locator.getPort());
        msg.setUserId(userId);

        mqClient.publish(getKickUserTopic(locator.getHostAndPort()), msg.encode());
    }

    private void receiveKickRemoteMsg(KickMqMessage msg) {
        if (!piccoloServer.isTargetMachine(msg.getTargetAddress(), msg.getTargetPort())) {
            LOGGER.error("receive kick remote msg, target server error, address: {} port: {}", msg.getTargetAddress(), msg.getTargetPort());
            return;
        }

        RouterCenter routerCenter = piccoloServer.getRouterCenter();
        LocalRouter localRouter = routerCenter.lookupLocal(msg.getUserId(), msg.getClientType());
        if (localRouter != null) {
            LOGGER.info("receive kick remote msg, msg: {}", msg);
            if (StringUtils.equals(localRouter.getRouterValue().getId(), msg.getConnId())) {
                sendKickUserMessageToClient(msg.getUserId(), localRouter);
            } else {
                LOGGER.warn("kick router failure target connId not match, localRouter: {}, msg: {}", localRouter, msg);
            }
        } else {
            LOGGER.error("kick router failure can not found local router, msg: {}", msg);
        }
    }

    @Override
    public void receive(String topic, Object message, Object... attachment) {
        if (kickUserTopic.equals(topic)) {
            byte[] data = (byte[]) message;
            KickMqMessage kickRemoteMsg = Jsons.fromJson(new String(data, StandardCharsets.UTF_8), KickMqMessage.class);
            if (kickRemoteMsg != null) {
                receiveKickRemoteMsg(kickRemoteMsg);
            }
        }
    }

    public static String getKickUserTopic(String hostAndPort) {
        return MQTopic.getTopic("kick." + hostAndPort.replaceAll(":", "."));
    }
}
