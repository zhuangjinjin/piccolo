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

import io.github.ukuz.piccolo.api.connection.Connection;
import io.github.ukuz.piccolo.api.event.RouterChangeEvent;
import io.github.ukuz.piccolo.api.router.ClientLocator;
import io.github.ukuz.piccolo.api.service.AbstractService;
import io.github.ukuz.piccolo.api.service.ServiceException;
import io.github.ukuz.piccolo.common.event.EventBus;
import io.github.ukuz.piccolo.common.router.RemoteRouter;
import io.github.ukuz.piccolo.common.router.RemoteRouterManager;
import io.github.ukuz.piccolo.core.PiccoloServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * @author ukuz90
 */
public final class RouterCenter extends AbstractService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RouterCenter.class);

    private LocalRouterManager localRouterManager;
    private RemoteRouterManager remoteRouterManager;
    private RouterChangeListener routerChangeListener;
    private UserEventListener userEventListener;

    private PiccoloServer piccoloServer;

    public RouterCenter(PiccoloServer piccoloServer) {
        this.piccoloServer = piccoloServer;
        localRouterManager = new LocalRouterManager();
        remoteRouterManager = new RemoteRouterManager(piccoloServer.getCacheManager());
        routerChangeListener = new RouterChangeListener(piccoloServer);
        userEventListener = new UserEventListener(piccoloServer);
    }

    @Override
    public void init() throws ServiceException {
        LOGGER.info("router center init.");
        routerChangeListener.init();
        userEventListener.getUserManager().clearOnlineList();
    }

    /**
     * register
     *
     * @param userId
     * @param connection
     * @return
     */
    public boolean register(String userId, Connection connection) {
        ClientLocator clientLocator = ClientLocator.from(connection)
                .setHost(piccoloServer.getGatewayServer().getRegistration().getHost())
                .setPort(piccoloServer.getGatewayServer().getRegistration().getPort());

        LocalRouter localRouter = new LocalRouter(connection);
        RemoteRouter remoteRouter = new RemoteRouter(clientLocator);

        LocalRouter oldLocalRouter = null;
        RemoteRouter oldRemoteRouter = null;
        try {
            oldLocalRouter = localRouterManager.register(userId, localRouter);
            oldRemoteRouter = remoteRouterManager.register(userId, remoteRouter);
        } catch (Exception e) {
            LOGGER.error("register router failure, userId: {}, cause: {}", userId, e);
        }

        if (oldLocalRouter != null) {
            EventBus.post(new RouterChangeEvent(userId, oldLocalRouter));
            LOGGER.info("register router success, userId: {}, oldLocalRouter: {}", oldLocalRouter);
        }

        if (oldRemoteRouter != null && oldRemoteRouter.isOnline()) {
            EventBus.post(new RouterChangeEvent(userId, oldRemoteRouter));
            LOGGER.info("register router success, userId: {}, oldRemoteRouter: {}", oldRemoteRouter);
        }

        return true;
    }

    @Override
    public void destroy() throws ServiceException {
        userEventListener.getUserManager().clearOnlineList();
    }

    /**
     * unregister
     *
     * @param userId
     * @param clientType
     * @return
     */
    public boolean unRegister(String userId, byte clientType) {
        return unRegisterRemote(userId, clientType) && unRegisterLocal(userId, clientType);
    }

    public LocalRouter lookupLocal(String userId, byte clientType) {
        return localRouterManager.lookup(userId, clientType);
    }

    public Set<LocalRouter> lookupLocal(String userId) {
        return localRouterManager.lookupAll(userId);
    }

    public Set<LocalRouter> lookupLocalAll() {
        return localRouterManager.lookupAll();
    }

    public RemoteRouter lookupRemote(String userId, byte clientType) {
        return remoteRouterManager.lookup(userId, clientType);
    }

    public Set<RemoteRouter> lookupRemote(String userId) {
        return remoteRouterManager.lookupAll(userId);
    }

    public boolean unRegisterLocal(String userId, byte clientType) {
        return localRouterManager.unregister(userId, clientType);
    }

    public boolean unRegisterRemote(String userId, byte clientType) {
        return remoteRouterManager.unregister(userId, clientType);
    }
}
