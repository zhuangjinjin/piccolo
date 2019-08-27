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

import com.google.common.base.Strings;
import io.github.ukuz.piccolo.api.PiccoloContext;
import io.github.ukuz.piccolo.api.connection.Connection;
import io.github.ukuz.piccolo.api.connection.SessionContext;
import io.github.ukuz.piccolo.api.event.UserOfflineEvent;
import io.github.ukuz.piccolo.api.event.UserOnlineEvent;
import io.github.ukuz.piccolo.api.exchange.ExchangeException;
import io.github.ukuz.piccolo.api.exchange.handler.ChannelHandler;
import io.github.ukuz.piccolo.api.exchange.handler.ChannelHandlerDelegateAdapter;
import io.github.ukuz.piccolo.common.event.EventBus;
import io.github.ukuz.piccolo.common.message.BindUserMessage;
import io.github.ukuz.piccolo.common.message.ErrorMessage;
import io.github.ukuz.piccolo.common.message.OkMessage;
import io.github.ukuz.piccolo.common.message.UnbindUserMessage;
import io.github.ukuz.piccolo.common.router.RemoteRouter;
import io.github.ukuz.piccolo.core.PiccoloServer;
import io.github.ukuz.piccolo.core.router.LocalRouter;
import io.github.ukuz.piccolo.core.router.RouterCenter;
import io.netty.util.internal.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ukuz90
 */
public class BindUserHandler extends ChannelHandlerDelegateAdapter {

    private final Logger logger = LoggerFactory.getLogger(BindUserHandler.class);

    public BindUserHandler(PiccoloContext piccoloContext, ChannelHandler handler) {
        super(piccoloContext, handler);
    }

    @Override
    public void received(Connection connection, Object message) throws ExchangeException {
        if (message instanceof BindUserMessage || message instanceof UnbindUserMessage) {
            try {
                if (message instanceof BindUserMessage) {
                    bind(connection, (BindUserMessage)message);
                } else if (message instanceof UnbindUserMessage) {
                    unbind(connection, (UnbindUserMessage) message);
                }

            } catch (Exception e) {
                throw new ExchangeException(e);
            }
        } else {
            super.received(connection, message);
        }
    }

    private void bind(Connection connection, BindUserMessage msg) throws Exception {
        if (StringUtil.isNullOrEmpty(msg.userId)) {
            connection.sendAsyncAndClose(ErrorMessage.build(msg).reason("invalid param, userId must not be null"));
            logger.error("invalid param, userId must not be null， conn: {}", connection);
            throw new IllegalArgumentException("invalid param, userId must not be null");
        }

        //1. 绑定用户前先判断是否握手成功
        SessionContext context = connection.getSessionContext();
        if (context.handshakeOk()) {
            //处理重复绑定问题
            if (context.getUserId() != null) {
                if (context.getUserId().equals(msg.userId)) {
                    context.setTags(msg.tags);
                    connection.sendAsync(OkMessage.build(msg));
                    logger.info("bind user success, userId: {} conn: {}", msg.userId, connection);
                    return;
                } else {
                    unbind(connection, UnbindUserMessage.from(connection, msg));
                }
            }

            //TODO 验证用户身份


            //向路由中心注册用户
            RouterCenter routerCenter = ((PiccoloServer)piccoloContext).getRouterCenter();
            boolean success = routerCenter.register(msg.userId, connection);

            if (success) {
                context.setUserId(msg.userId);
                context.setTags(msg.tags);
                EventBus.post(new UserOnlineEvent(msg.userId, connection));
                connection.sendAsync(OkMessage.build(msg).data("bind success"));
                logger.info("bind success, userId: {} conn: {}", msg.userId, connection);
            } else {
                //若注册失败，则注销用户
                routerCenter.unRegister(msg.userId, context.getClientType());
                connection.sendAsyncAndClose(ErrorMessage.build(msg).reason("bind failure"));
                logger.info("bind failure, userId: {} conn: {}", msg.userId, connection);
            }

        } else {
            connection.sendAsyncAndClose(ErrorMessage.build(msg).reason("not handshake"));
            logger.error("not handshake， conn: {}", connection);
            throw new IllegalArgumentException("not handshake");
        }
    }

    private void unbind(Connection connection, UnbindUserMessage msg) {
        if (Strings.isNullOrEmpty(msg.userId)) {
            connection.sendAsyncAndClose(ErrorMessage.build(msg).reason("invalid param"));
            logger.error("unbind user failure or invalid param, conn: {}", connection);
            return;
        }
        //1.解绑用户时先看下是否握手成功
        SessionContext context = connection.getSessionContext();
        if (context.handshakeOk()) {
            String userId = msg.userId;
            byte clientType = context.getClientType();
            //2.先删除远程路由, 必须是同一个设备才允许解绑
            boolean success = true;
            RouterCenter routerCenter = ((PiccoloServer)piccoloContext).getRouterCenter();
            RemoteRouter remoteRouter = routerCenter.lookupRemote(userId, clientType);
            if (remoteRouter != null) {
                String deviceId = remoteRouter.getRouterValue().getDeviceId();
                if (context.getDeviceId().equals(deviceId)) {
                    success = routerCenter.unRegisterRemote(userId, clientType);
                }
            }

            //3.删除本地路由信息
            if (success) {
                LocalRouter localRouter = routerCenter.lookupLocal(userId, clientType);
                if (localRouter != null) {
                    String deviceId = localRouter.getRouterValue().getSessionContext().getDeviceId();
                    if (context.getDeviceId().equals(deviceId)) {
                        success = routerCenter.unRegisterLocal(userId, clientType);
                    }
                }
            }

            //4.路由删除成功，广播用户下线事件
            if (success) {
                context.setUserId(null);
                context.setTags(null);
                connection.sendAsync(OkMessage.build(msg).data("unbind success"));
                EventBus.post(new UserOfflineEvent(userId, connection));
                logger.info("unbind success, userId: {} conn: {}", userId, connection);
            } else {
                connection.sendAsyncAndClose(ErrorMessage.build(msg).reason("unbind failure"));
                logger.error("unbind failure, userId: {} conn: {}", userId, connection);
            }
        } else {
            connection.sendAsyncAndClose(ErrorMessage.build(msg).reason("not handshake"));
            logger.error("not handshake, conn: {}", connection);
        }

    }
}
