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
package io.github.ukuz.piccolo.transport.connection;

import io.github.ukuz.piccolo.api.config.Environment;
import io.github.ukuz.piccolo.api.connection.Connection;
import io.github.ukuz.piccolo.api.connection.SessionContext;
import io.github.ukuz.piccolo.api.exchange.support.BaseMessage;
import io.github.ukuz.piccolo.common.properties.SecurityProperties;
import io.github.ukuz.piccolo.common.security.RSACipher;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ukuz90
 */
public class NettyConnection implements Connection, ChannelFutureListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(NettyConnection.class);

    private Channel channel;
    private long lastWriteTime;
    private long lastReadTime;
    private SessionContext context;
    private byte state = STATE_NEW;
    private Environment environment;


    public NettyConnection(Environment environment) {
        this.environment = environment;
        this.context = new SessionContext();
    }

    @Override
    public void init(Channel channel, boolean isSecurity) {
        if (!channel.isActive()) {
            throw new IllegalArgumentException("channel is not active, state: {}");
        }
        this.channel = channel;
        if (isSecurity) {
            SecurityProperties security = environment.getProperties(SecurityProperties.class);
            this.context.changeCipher(new RSACipher(security.getPublicKey(), security.getPrivateKey()));
        }
    }

    @Override
    public Channel getChannel() {
        return channel;
    }

    @Override
    public boolean sendSync(BaseMessage message) {
        try {
            channel.writeAndFlush(message).addListener(this).sync();
            return true;
        } catch (InterruptedException e) {
            return false;
        }
    }

    @Override
    public ChannelFuture sendAsync(BaseMessage message) {
        return sendAsync(message, null);
    }

    @Override
    public ChannelFuture sendRawAsync(BaseMessage message) {
       return sendRawAsync(message, null);
    }

    @Override
    public ChannelFuture sendRawAsync(BaseMessage message, ChannelFutureListener listener) {
        // not encrypt, not compress
        message.setRaw(true);
        return sendAsync(message, listener);
    }

    @Override
    public ChannelFuture sendAsyncAndClose(BaseMessage message) {
        return sendAsync(message, ChannelFutureListener.CLOSE);
    }

    @Override
    public ChannelFuture sendAsync(BaseMessage message, ChannelFutureListener listener) {
        if (channel.isActive()) {
            ChannelFuture future = channel.writeAndFlush(message).addListener(this);

            if (listener != null) {
                future.addListener(listener);
            }

            if (channel.isWritable()) {
                return future;
            }

            if (!channel.eventLoop().inEventLoop()) {
                future.awaitUninterruptibly(100);
            }

            return future;
        } else {
            return this.close();
        }

    }

    @Override
    public SessionContext getSessionContext() {
        return context;
    }

    @Override
    public void setSessionContext(SessionContext sessionContext) {
        this.context = sessionContext;
    }

    @Override
    public ChannelFuture close() {
        if (state == STATE_CLOSED) {
            return null;
        }
        state = STATE_CLOSED;
        return channel.close();
    }

    @Override
    public boolean isClosed() {
        return state == STATE_CLOSED;
    }

    @Override
    public boolean isConnected() {
        return state == STATE_CONNECTED;
    }

    @Override
    public boolean isReadTimeout() {
        return System.currentTimeMillis() - lastReadTime > context.getHeartbeat();
    }

    @Override
    public boolean isWriteTimeout() {
        return System.currentTimeMillis() - lastWriteTime > context.getHeartbeat();
    }

    @Override
    public void updateLastReadTime() {
        lastReadTime = System.currentTimeMillis();
    }

    @Override
    public void updateLastWriteTime() {
        lastWriteTime = System.currentTimeMillis();
    }

    @Override
    public String getId() {
        return channel.id().asShortText();
    }

    @Override
    public void operationComplete(ChannelFuture future) throws Exception {
        if (future.isSuccess()) {
            lastWriteTime = System.currentTimeMillis();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("connection send msg success, channel: {}", future.channel());
            }
        } else {
            LOGGER.error("connection send msg error: cause: {}", future.cause().getMessage());
        }
    }
}
