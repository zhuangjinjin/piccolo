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
import io.github.ukuz.piccolo.api.cache.CacheManager;
import io.github.ukuz.piccolo.api.connection.Connection;
import io.github.ukuz.piccolo.api.connection.SessionContext;
import io.github.ukuz.piccolo.api.event.ConnectionCloseEvent;
import io.github.ukuz.piccolo.api.router.ClientLocator;
import io.github.ukuz.piccolo.api.router.RouterManager;
import io.github.ukuz.piccolo.common.cache.CacheKeys;
import io.github.ukuz.piccolo.common.event.EventObservable;
import io.netty.util.internal.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author ukuz90
 */
public class RemoteRouterManager extends EventObservable implements RouterManager<RemoteRouter> {

    private final Logger logger = LoggerFactory.getLogger(RemoteRouterManager.class);
    private CacheManager cacheManager;

    public RemoteRouterManager(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Override
    public RemoteRouter register(String userId, RemoteRouter router) {
        String key = CacheKeys.getUserRouteKey(userId);
        String field = String.valueOf(router.getClientType());
        //TODO need atomic?
        ClientLocator old = cacheManager.hget(key, field, ClientLocator.class);
        cacheManager.hset(key, field, router);
        return old == null ? null : new RemoteRouter(old);
    }

    @Override
    public boolean unregister(String userId, byte clientType) {
        String key = CacheKeys.getUserRouteKey(userId);
        String field = String.valueOf(clientType);
        ClientLocator old = null;
        try {
            old = cacheManager.hget(key, field, ClientLocator.class);
            if (old == null || old.isOffline()) {
                return true;
            }
            cacheManager.hset(key, field, old.offline().toJson());
            logger.info("unRegister remote router success,  userId: {} router: {}", userId, old);
            return true;
        } catch (Exception e) {
            logger.error("unRegister remote router failure, userId: {} router: {} cause: {}", userId, old, e);
            return false;
        }
    }

    @Override
    public RemoteRouter lookup(String userId, byte clientType) {
        String key = CacheKeys.getUserRouteKey(userId);
        String field = String.valueOf(clientType);
        ClientLocator clientLocator = cacheManager.hget(key, field, ClientLocator.class);
        logger.info("lookup remote router userId={}, router={}", userId, clientLocator);
        return clientLocator == null ? null : new RemoteRouter(clientLocator);
    }

    @Override
    public Set<RemoteRouter> lookupAll(String userId) {
        String key = CacheKeys.getUserRouteKey(userId);
        Map<String, ClientLocator> map = cacheManager.hgetAll(key, ClientLocator.class);
        if (map == null || map.isEmpty()) {
            return Collections.emptySet();
        }
        return map.values().stream().map(RemoteRouter::new).collect(Collectors.toSet());
    }

    @Subscribe
    @AllowConcurrentEvents
    public void on(ConnectionCloseEvent event) {
        Connection connection = event.getConnection();
        if (connection == null) {
            return;
        }
        SessionContext context = connection.getSessionContext();
        if (StringUtil.isNullOrEmpty(context.getUserId())) {
            return;
        }
        String key = CacheKeys.getUserRouteKey(context.getUserId());
        String field = String.valueOf(context.getClientType());
        ClientLocator clientLocator = cacheManager.hget(key, field, ClientLocator.class);
        if (clientLocator == null || clientLocator.isOffline()) {
            return;
        }
        //检测是否同一个链接，
        if (connection.getId().equals(clientLocator.getConnId())) {
            cacheManager.hset(key, field, clientLocator.offline().toJson());
            logger.info("clean disconnected remote route, userId: {}, route: {}", context.getUserId(), clientLocator);
        } else {
            logger.info("clean disconnected remote route, not clean, userId: {}, route: {}", context.getUserId(), clientLocator);
        }

    }
}
