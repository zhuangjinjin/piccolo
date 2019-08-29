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
package io.github.ukuz.piccolo.client.connect;

import io.github.ukuz.piccolo.api.cache.CacheManager;
import io.github.ukuz.piccolo.api.config.Environment;
import io.github.ukuz.piccolo.api.connection.Cipher;
import io.github.ukuz.piccolo.api.connection.Connection;
import io.github.ukuz.piccolo.api.connection.SessionContext;
import io.github.ukuz.piccolo.api.exchange.ExchangeException;
import io.github.ukuz.piccolo.api.exchange.handler.ChannelHandler;
import io.github.ukuz.piccolo.api.exchange.support.BaseMessage;
import io.github.ukuz.piccolo.api.spi.SpiLoader;
import io.github.ukuz.piccolo.client.properties.ClientProperties;
import io.github.ukuz.piccolo.common.cache.CacheKeys;
import io.github.ukuz.piccolo.common.message.*;
import io.github.ukuz.piccolo.common.security.AESCipher;
import io.github.ukuz.piccolo.common.security.CipherBox;
import io.github.ukuz.piccolo.common.security.RSACipher;
import io.netty.util.AttributeKey;
import io.netty.util.internal.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author ukuz90
 */
public class ConnectClientHandler implements ChannelHandler {

    private static final SimpleStatistics STATISTICS = new SimpleStatistics();
    private final Logger logger = LoggerFactory.getLogger(ConnectClientHandler.class);

    public static final AttributeKey<ClientConfig> CONFIG_KEY = AttributeKey.newInstance("config_key");
    private ClientConfig clientConfig;
    private ClientProperties properties;
    private CacheManager cacheManager = SpiLoader.getLoader(CacheManager.class).getExtension();

    private boolean isPerformanceTest;

    public ConnectClientHandler(Environment environment) {
        isPerformanceTest = true;
        properties = environment.getProperties(ClientProperties.class);
    }

    public ConnectClientHandler(Environment environment, ClientConfig clientConfig) {
        this.clientConfig = clientConfig;
        properties = environment.getProperties(ClientProperties.class);
    }

    @Override
    public void connected(Connection connection) throws ExchangeException {
        for (int i = 0; i < 3; i++) {
            if (clientConfig != null) {
                break;
            }
            clientConfig = connection.getChannel().attr(CONFIG_KEY).get();
            if (clientConfig == null) {
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    throw new ExchangeException(e);
                }
            }
        }
        if (clientConfig == null) {
            throw new ExchangeException("clientConfig must not be null, connection: " + connection);
        }
        if (isPerformanceTest) {
            handshake(connection);
        } else {
            fastConnect(connection);
        }
    }

    @Override
    public void disconnected(Connection connection) throws ExchangeException {

    }

    @Override
    public void sent(Connection connection, Object message) throws ExchangeException {

    }

    @Override
    public void received(Connection connection, Object message) throws ExchangeException {
        if (!(message instanceof BaseMessage)) {
            logger.warn("message was invalid wire type, message: {} conn: {}", message, connection);
            return;
        }
        SessionContext context = connection.getSessionContext();

        if (message instanceof HandshakeOkMessage) {
            HandshakeOkMessage msg = (HandshakeOkMessage) message;
            int connectedNum = STATISTICS.increaseConnectedNum();
            byte[] sessionKey = CipherBox.I.mixKey(clientConfig.getClientKey(), msg.serverKey);

            context.changeCipher(new AESCipher(sessionKey, clientConfig.getIv()));
            context.setHeartbeat(msg.heartbeat);
            startHeartbeat(msg.heartbeat - 1000);

            bindUser(connection);
            if (!isPerformanceTest) {
                saveFastConnectionInfo(msg, context.getCipher());
            }
            logger.info("handshake success, clientConfig: {} conn: {} connectedNum: {}", clientConfig, connection, connectedNum);

        } else if (message instanceof FastConnectOkMessage) {
            FastConnectOkMessage msg = (FastConnectOkMessage) message;
            int connectedNum = STATISTICS.increaseConnectedNum();
            String cipherStr = clientConfig.getCipher();
            String[] cs = cipherStr.split(",");
            byte[] key = AESCipher.toArray(cs[0]);
            byte[] iv = AESCipher.toArray(cs[1]);

            connection.getSessionContext().changeCipher(new AESCipher(key, iv));
            context.setHeartbeat(msg.heartbeat);
            startHeartbeat(msg.heartbeat - 1000);


            bindUser(connection);
            logger.info("fast connect success, clientConfig: {} conn: {} connectedNum: {}", clientConfig, connection, connectedNum);
        } else if (message instanceof ErrorMessage) {
            logger.error("receive of error message: {}", message);
        }
    }

    private void handshake(Connection connection) {
        SessionContext context = connection.getSessionContext();
        context.changeCipher(new RSACipher(properties.getServerPublicKey(), properties.getClientPrivateKey()));
        HandshakeMessage message = new HandshakeMessage(connection);
        message.clientKey = clientConfig.getClientKey();
        message.iv = clientConfig.getIv();
        message.clientVersion = clientConfig.getClientVersion();
        message.deviceId = clientConfig.getDeviceId();
        message.osName = clientConfig.getOsName();
        message.osVersion = clientConfig.getOsVersion();
        message.timestamp = System.currentTimeMillis();
        connection.sendAsync(message, future -> {
            if (future.isSuccess()) {
                //切换RSA=>AES(ClientKey)
                context.changeCipher(new AESCipher(message.clientKey, message.iv));
                logger.info("handshake success, message: {} conn: {}", message, connection);
            } else {
                logger.warn("handshake failure, message: {} conn: {} cause: {}", message, connection, future.cause());
            }
        });
        logger.debug("send handshake message: {} conn: {}", message, connection);
    }

    private void fastConnect(Connection connection) {
        SessionContext context = connection.getSessionContext();
        Map<String, String> session = getFastConnectionInfo(clientConfig.getDeviceId());
        if (session == null) {
            handshake(connection);
            return;
        }
        String sessionId = session.get("sessionId");
        if (StringUtil.isNullOrEmpty(sessionId)) {
            handshake(connection);
            return;
        }
        String expireTime = session.get("expireTime");
        if (!StringUtil.isNullOrEmpty(expireTime)) {
            long expire = Long.parseLong(expireTime);
            if (expire < System.currentTimeMillis()) {
                handshake(connection);
                return;
            }
        }

        String cipherStr = session.get("cipherStr");

        FastConnectMessage message = new FastConnectMessage(connection);
        message.deviceId = clientConfig.getDeviceId();
        message.sessionId = sessionId;

        connection.sendRawAsync(message, future -> {
            if (future.isSuccess()) {
                clientConfig.setCipher(cipherStr);
            } else {
                handshake(connection);
            }
        });

    }

    private void saveFastConnectionInfo(HandshakeOkMessage message, Cipher cipher) {
        Map<String, String> result = new HashMap<>(3);
        result.put("expireTime", String.valueOf(message.expireTime));
        result.put("sessionId", message.sessionId);
        result.put("cipher", cipher.toString());
        String key = CacheKeys.getDeviceIdKey(clientConfig.getDeviceId());
        cacheManager.set(key, result, 60 * 5);
    }

    private Map<String, String> getFastConnectionInfo(String deviceId) {
        String key = CacheKeys.getDeviceIdKey(deviceId);
        return cacheManager.get(key, Map.class);
    }

    private void bindUser(Connection connection) {
        BindUserMessage message = new BindUserMessage(connection);
        message.userId = clientConfig.getUserId();
        message.tags = "test";
        connection.sendAsync(message);

        connection.getSessionContext().setUserId(clientConfig.getUserId());
    }

    private void startHeartbeat(int heartbeat) {

    }

    private boolean healthCheck() {
        return false;
    }

    @Override
    public void caught(Connection connection, Throwable exception) throws ExchangeException {

    }
}
