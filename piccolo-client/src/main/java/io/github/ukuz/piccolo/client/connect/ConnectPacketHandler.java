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
import io.github.ukuz.piccolo.api.connection.Connection;
import io.github.ukuz.piccolo.api.connection.SessionContext;
import io.github.ukuz.piccolo.api.exchange.ExchangeException;
import io.github.ukuz.piccolo.api.exchange.handler.ChannelHandler;
import io.github.ukuz.piccolo.api.exchange.support.BaseMessage;
import io.github.ukuz.piccolo.api.spi.SpiLoader;
import io.github.ukuz.piccolo.client.properties.ClientProperties;
import io.github.ukuz.piccolo.common.cache.CacheKeys;
import io.github.ukuz.piccolo.common.event.EventBus;
import io.github.ukuz.piccolo.common.message.*;
import io.github.ukuz.piccolo.common.security.AESCipher;
import io.github.ukuz.piccolo.common.security.RSACipher;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author ukuz90
 */
public class ConnectPacketHandler implements ChannelHandler {

    private final Logger logger = LoggerFactory.getLogger(ConnectPacketHandler.class);

    public static final AttributeKey<ClientConfig> CONFIG_KEY = AttributeKey.newInstance("config_key");
    private ClientConfig clientConfig;
    private ClientProperties properties;
    private CacheManager cacheManager = SpiLoader.getLoader(CacheManager.class).getExtension();

    private boolean isPerformanceTest;

    public ConnectPacketHandler(Environment environment) {
        isPerformanceTest = true;
        properties = environment.getProperties(ClientProperties.class);
    }

    public ConnectPacketHandler(Environment environment, ClientConfig clientConfig) {
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
        BaseMessage msg = (BaseMessage) message;
        if (msg instanceof HandshakeOkMessage) {

        } else if (msg instanceof FastConnectOkMessage) {

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
                context.changeCipher(new AESCipher(properties.getAesKeyLength(), message.clientKey, message.iv));
                logger.info("handshake success, message: {} conn: {}", message, connection);
            } else {
                logger.warn("handshake failure, message: {} conn: {} cause: {}", message, connection, future.cause());
            }
        });
        logger.debug("send handshake message: {} conn: {}", message, connection);
    }

    private void fastConnect(Connection connection) {
        SessionContext context = connection.getSessionContext();
        FastConnectMessage message = new FastConnectMessage(connection);
        message.deviceId = clientConfig.getDeviceId();
        Map<String, String> info = getFastConnectionInfo(clientConfig.getDeviceId());
        message.sessionId = info.get("sessionId");

    }

    private void saveFastConnectionInfo(HandshakeOkMessage message) {
        Map<String, String> result = new HashMap<>();
        result.put("expireTime", String.valueOf(message.expireTime));
        result.put("sessionId", message.sessionId);
        String key = CacheKeys.getDeviceIdKey(clientConfig.getDeviceId());
        cacheManager.set(key, result, 60 * 5);
    }

    private Map<String, String> getFastConnectionInfo(String deviceId) {
        String key = CacheKeys.getDeviceIdKey(deviceId);
        return cacheManager.get(key, Map.class);
    }

    private void bindUser() {

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
