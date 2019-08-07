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

import io.github.ukuz.piccolo.api.connection.Connection;
import io.github.ukuz.piccolo.api.connection.SessionContext;
import io.github.ukuz.piccolo.api.exchange.ExchangeException;
import io.github.ukuz.piccolo.api.exchange.handler.ChannelHandler;
import io.github.ukuz.piccolo.api.exchange.handler.ChannelHandlerDelegateAdapter;
import io.github.ukuz.piccolo.common.message.HandshakeMessage;
import io.github.ukuz.piccolo.common.security.AESCipher;
import io.github.ukuz.piccolo.common.security.CipherBox;
import io.github.ukuz.piccolo.common.security.RSACipher;
import io.netty.util.internal.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ukuz90
 */
public class HandshakeServerHandler extends ChannelHandlerDelegateAdapter {

    private final Logger logger = LoggerFactory.getLogger(HandshakeServerHandler.class);

    public HandshakeServerHandler(ChannelHandler handler) {
        super(handler);
    }

    @Override
    public void connected(Connection connection) throws ExchangeException {
        connection.getSessionContext().changeCipher(new RSACipher("", ""));
        super.connected(connection);
    }

    @Override
    public void received(Connection connection, Object message) throws ExchangeException {
        if (message instanceof HandshakeMessage) {
            HandshakeMessage msg = (HandshakeMessage) message;
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

                //TODO send error msg and close

                logger.error("handshake failure, invalid, message: {}, conn: {}", msg, connection);
                return;
            }

            //2. 校验重复握手
            SessionContext context = connection.getSessionContext();
            if (msg.deviceId.equals(context.getDeviceId())) {

                //TODO send error msg

                logger.warn("handshake failure, repeat handshake, message: {}, conn: {}", msg, connection);
                return;
            }

            //3. 更换为对称加密算法 RSA=>AES
            context.changeCipher(new AESCipher(clientKey, iv));

            //4. 生成可复用的session, 用于快速重连

            //5. 计算心跳时间

            //6. 响应握手成功信息


        }



        super.received(connection, message);
    }


}
