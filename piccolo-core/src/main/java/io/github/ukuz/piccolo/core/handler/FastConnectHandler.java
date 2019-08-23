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
import io.github.ukuz.piccolo.common.ErrorCode;
import io.github.ukuz.piccolo.common.message.ErrorMessage;
import io.github.ukuz.piccolo.common.message.FastConnectMessage;
import io.github.ukuz.piccolo.common.message.FastConnectOkMessage;
import io.github.ukuz.piccolo.common.properties.CoreProperties;
import io.github.ukuz.piccolo.core.PiccoloServer;
import io.github.ukuz.piccolo.core.session.ReusableSession;
import io.github.ukuz.piccolo.core.session.ReusableSessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ukuz90
 */
public class FastConnectHandler extends ChannelHandlerDelegateAdapter {

    private final Logger logger = LoggerFactory.getLogger(FastConnectHandler.class);

    public FastConnectHandler(PiccoloContext piccoloContext, ChannelHandler handler) {
        super(piccoloContext, handler);
    }

    @Override
    public void received(Connection connection, Object message) throws ExchangeException {
        if (message instanceof FastConnectMessage) {
            FastConnectMessage msg = (FastConnectMessage) message;
            ReusableSessionManager reusableSessionManager = ((PiccoloServer)piccoloContext).getReusableSessionManager();
            ReusableSession session = reusableSessionManager.querySession(msg.sessionId);
            if (session == null) {

                //1.没查到说明session已经失效了
                connection.sendAsync(ErrorMessage.build(msg).code(ErrorCode.REPEAT_HANDSHAKE));
                logger.warn("fast connect failure, session was expired, sessionId: {} deviceId: {} conn: {}",
                        msg.sessionId, msg.deviceId, connection);

            } else if (!session.getContext().getDeviceId().equals(msg.deviceId)) {

                //2.非法的设备, 当前设备不是上次生成session时的设备
                connection.sendAsync(ErrorMessage.build(msg).code(ErrorCode.INVALID_DEVICE));
                logger.warn("fast connect failure, not the same device, sessionId: {} deviceId: {} conn: {}",
                        msg.sessionId, msg.deviceId, connection);

            } else {

                //3.校验成功，重新计算心跳，完成快速重连
                CoreProperties core = piccoloContext.getProperties(CoreProperties.class);
                int heartbeat = Math.max(core.getMinHeartbeatTime(), Math.min(msg.maxHeartbeat, core.getMaxHeartbeatTime()));

                FastConnectOkMessage okMessage = FastConnectOkMessage.build(connection)
                        .heartbeat(heartbeat);

                //不加密发送
                connection.sendRawAsync(okMessage, future -> {
                    if (future.isSuccess()) {
                        //4. 恢复缓存的会话信息(包含会话密钥等)
                        connection.setSessionContext(session.getContext());
                        logger.info("fast connect success, session: {} conn: {}", session.getContext(), connection);
                    } else {
                        logger.warn("fast connect failure, session: {} conn: {} cause: {}", session.getContext(), connection, future.cause());
                    }
                });

            }
        } else {
            super.received(connection, message);
        }
    }
}
