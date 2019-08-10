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

import io.github.ukuz.piccolo.api.PiccoloContext;
import io.github.ukuz.piccolo.api.connection.Connection;
import io.github.ukuz.piccolo.api.router.ClientLocator;
import io.github.ukuz.piccolo.api.router.Router;
import io.github.ukuz.piccolo.api.service.AbstractService;
import io.github.ukuz.piccolo.api.service.ServiceException;
import io.github.ukuz.piccolo.core.PiccoloServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

/**
 * @author ukuz90
 */
public final class RouterCenter extends AbstractService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RouterCenter.class);

    private LocalRouterManager localRouterManager;
    private RemoteRouterManager remoteRouterManager;

    private PiccoloServer piccoloServer;

    public RouterCenter(PiccoloServer piccoloServer) {
        this.piccoloServer = piccoloServer;
    }

    @Override
    public void init() throws ServiceException {
        LOGGER.info("router center init.");
        localRouterManager = new LocalRouterManager();
        remoteRouterManager = new RemoteRouterManager(piccoloServer.getCacheManager());
    }

    /**
     * register
     *
     * @param userId
     * @param connection
     * @return
     */
    public boolean register(String userId, Connection connection) {
//        ClientLocator clientLocator = ClientLocator.from(connection)
//                .setHost()
//                .setPort()
        return false;
    }

    /**
     * unregister
     *
     * @param userId
     * @param clientType
     * @return
     */
    public boolean unRegister(String userId, int clientType) {
        return false;
    }

}
