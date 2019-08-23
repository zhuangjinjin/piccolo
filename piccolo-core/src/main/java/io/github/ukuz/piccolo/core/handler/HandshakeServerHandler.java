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
import io.github.ukuz.piccolo.api.connection.SessionContext;
import io.github.ukuz.piccolo.api.exchange.ExchangeException;
import io.github.ukuz.piccolo.api.exchange.handler.ChannelHandler;
import io.github.ukuz.piccolo.api.exchange.handler.ChannelHandlerDelegateAdapter;
import io.github.ukuz.piccolo.common.ErrorCode;
import io.github.ukuz.piccolo.common.message.ErrorMessage;
import io.github.ukuz.piccolo.common.message.HandshakeMessage;
import io.github.ukuz.piccolo.common.message.HandshakeOkMessage;
import io.github.ukuz.piccolo.common.properties.CoreProperties;
import io.github.ukuz.piccolo.common.properties.SecurityProperties;
import io.github.ukuz.piccolo.common.security.AESCipher;
import io.github.ukuz.piccolo.common.security.CipherBox;
import io.github.ukuz.piccolo.common.security.RSACipher;
import io.github.ukuz.piccolo.core.PiccoloServer;
import io.github.ukuz.piccolo.core.session.ReusableSession;
import io.github.ukuz.piccolo.core.session.ReusableSessionManager;
import io.netty.util.internal.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ukuz90
 */
public class HandshakeServerHandler extends ChannelHandlerDelegateAdapter {

    private final Logger logger = LoggerFactory.getLogger(HandshakeServerHandler.class);

    public HandshakeServerHandler(PiccoloContext piccoloContext, ChannelHandler handler) {
        super(piccoloContext, handler);
    }

    @Override
    public void received(Connection connection, Object message) throws ExchangeException {
        if (message instanceof HandshakeMessage) {
            if (connection.getSessionContext().isSecurity()) {
                doInSecurity(connection, (HandshakeMessage) message);
            } else {
                doNotInSecurity(connection, (HandshakeMessage) message);
            }
        } else {
            super.received(connection, message);
        }
    }

    private void doInSecurity(Connection connection, HandshakeMessage msg) {
        //aes密钥向量16位
        byte[] iv = msg.iv;
        //客户端随机16位
        byte[] clientKey = msg.clientKey;
        //服务端随机16位
        byte[] serverKey = CipherBox.I.randomAESKey();
        //生成会话密钥16位
        byte[] sessionKey = CipherBox.I.mixKey(clientKey, serverKey);

        //1. 校验客户端参数
        if (StringUtil.isNullOrEmpty(msg.deviceId)
                || msg.iv.length != CipherBox.I.getAesKeyLength()
                || msg.clientKey.length != CipherBox.I.getAesKeyLength()) {

            //send error msg and close
            connection.sendAsyncAndClose(ErrorMessage.build(msg).reason("param invalid"));
            logger.error("handshake failure, invalid, message: {}, conn: {}", msg, connection);
            return;
        }

        //2. 校验重复握手
        SessionContext context = connection.getSessionContext();
        if (msg.deviceId.equals(context.getDeviceId())) {

            //send error msg
            connection.sendAsync(ErrorMessage.build(msg).code(ErrorCode.REPEAT_HANDSHAKE));

            logger.warn("handshake failure, repeat handshake, message: {}, conn: {}", msg, connection);
            return;
        }

        //3. 更换为对称加密算法 RSA=>AES(clientKey)
        SecurityProperties security = piccoloContext.getProperties(SecurityProperties.class);
        context.changeCipher(new AESCipher(security, clientKey, iv));

        //4. 生成可复用的session, 用于快速重连
        ReusableSessionManager reusableSessionManager = ((PiccoloServer)piccoloContext).getReusableSessionManager();
        ReusableSession session = reusableSessionManager.genSession(context);

        //5. 计算心跳时间
        CoreProperties core = piccoloContext.getProperties(CoreProperties.class);
        int heartbeat = Math.max(core.getMinHeartbeatTime(), Math.min(msg.maxHeartbeat, core.getMaxHeartbeatTime()));

        //6. 响应握手成功信息
        HandshakeOkMessage okMessage = HandshakeOkMessage.build(connection)
                .sessionId(session.getSessionId())
                .expireTime(session.getExpireTime())
                .serverKey(serverKey)
                .heartbeat(heartbeat);

        connection.sendAsync(okMessage, future -> {
            if (future.isSuccess()) {
                //7. 更换为对称加密算法 RSA=>AES(sessionKey)
                context.changeCipher(new AESCipher(security, sessionKey, iv));
                //8. 保存当前信息到SessionContext中
                connection.getSessionContext()
                        .setDeviceId(msg.deviceId)
                        .setClientVersion(msg.clientVersion)
                        .setOsVersion(msg.osVersion)
                        .setOsVersion(msg.osVersion)
                        .setHeartbeat(heartbeat);

                //9. 保存当前session到缓存中，用来快速重连
                reusableSessionManager.cacheSession(session);

                logger.info("handshake success, conn: {}", connection);
            } else {
                logger.info("handshake failure, conn: {}, cause: {}", connection, future.cause());
            }
        });
    }

    private void doNotInSecurity(Connection connection, HandshakeMessage msg) {

        //1. 校验客户端参数
        if (StringUtil.isNullOrEmpty(msg.deviceId)) {

            //send error msg and close
            connection.sendAsyncAndClose(ErrorMessage.build(msg).reason("param invalid"));
            logger.error("handshake failure, invalid, message: {}, conn: {}", msg, connection);
            return;
        }

        //2. 校验重复握手
        SessionContext context = connection.getSessionContext();
        if (msg.deviceId.equals(context.getDeviceId())) {

            //send error msg
            connection.sendAsync(ErrorMessage.build(msg).code(ErrorCode.REPEAT_HANDSHAKE));

            logger.warn("handshake failure, repeat handshake, message: {}, conn: {}", msg, connection);
            return;
        }

        //3. 计算心跳时间
        CoreProperties core = piccoloContext.getProperties(CoreProperties.class);
        int heartbeat = Math.max(core.getMinHeartbeatTime(), Math.min(msg.maxHeartbeat, core.getMaxHeartbeatTime()));

        //4. 响应握手成功信息
        HandshakeOkMessage okMessage = HandshakeOkMessage.build(connection);
        okMessage.heartbeat(heartbeat);
        connection.sendAsync(okMessage);

        connection.getSessionContext()
                .setDeviceId(msg.deviceId)
                .setClientVersion(msg.clientVersion)
                .setOsVersion(msg.osVersion)
                .setHeartbeat(Integer.MAX_VALUE);

        logger.info("handshake success, conn: {}", connection);
    }

}
