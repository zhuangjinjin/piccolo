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
import io.github.ukuz.piccolo.api.connection.Connection;
import io.github.ukuz.piccolo.api.connection.SessionContext;
import io.github.ukuz.piccolo.api.event.ConnectionCloseEvent;
import io.github.ukuz.piccolo.api.event.UserOfflineEvent;
import io.github.ukuz.piccolo.api.router.RouterManager;
import io.github.ukuz.piccolo.common.event.EventBus;
import io.github.ukuz.piccolo.common.event.EventObservable;
import io.netty.util.internal.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author ukuz90
 */
public class LocalRouterManager extends EventObservable implements RouterManager<LocalRouter> {

    private final Logger logger = LoggerFactory.getLogger(LocalRouterManager.class);

    private static final Map<Byte, LocalRouter> EMPTY = new HashMap<>(0);

    private ConcurrentMap<String, Map<Byte, LocalRouter>> routers = new ConcurrentHashMap<>();

    @Override
    public LocalRouter register(String userId, LocalRouter router) {
        logger.info("register userId: {} clientType: {} router: {}", userId, router.getClientType(), router);
        return routers.computeIfAbsent(userId, (s) -> new HashMap<>(1)).put(router.getClientType(), router);
    }

    @Override
    public boolean unregister(String userId, byte clientType) {
        LocalRouter router = routers.getOrDefault(userId, EMPTY).remove(clientType);
        logger.info("unregister userId: {} clientType: {} router: {}", userId, clientType, router);
        return true;
    }

    @Override
    public LocalRouter lookup(String userId, byte clientType) {
        LocalRouter router = routers.getOrDefault(userId, EMPTY).get(clientType);
        logger.info("lookup userId: {} clientType: {} router: {}", userId, clientType, router);
        return router;
    }

    @Override
    public Set<LocalRouter> lookupAll(String userId) {
        Set<LocalRouter> sets = new HashSet<>(routers.getOrDefault(userId, EMPTY).values());
        logger.info("lookupAll userId: {} router: {}", userId, sets);
        return sets;
    }

    public Set<LocalRouter> lookupAll() {
        Map<String, Map<Byte, LocalRouter>> routers = Collections.unmodifiableMap(this.routers);
        Set<LocalRouter> result = new HashSet<>();
        routers.forEach((k, v) -> result.addAll(v.values()));
        logger.info("lookupAll router size: {}", result.size());
        return result;
    }

    @Subscribe
    @AllowConcurrentEvents
    public void on(ConnectionCloseEvent event) {
        Connection connection = event.getConnection();
        if (connection == null) {
            return;
        }
        SessionContext context = connection.getSessionContext();
        String userId = context.getUserId();
        if (StringUtil.isNullOrEmpty(userId)) {
            logger.info("connection closed, userId is empty, context: {} conn: {}", context, connection);
            return;
        }

        byte clientType = context.getClientType();
        LocalRouter router = routers.getOrDefault(userId, EMPTY).get(clientType);
        if (router == null) {
            logger.info("connection closed, can not found router, clientType: {}", clientType);
            return;
        }
        String connId = connection.getId();
        //检测下，是否是同一个链接, 如果客户端重连，老的路由会被新的链接覆盖
        if (connId.equals(router.getRouterValue().getId())) {
            routers.getOrDefault(userId, EMPTY).remove(clientType);
            EventBus.post(new UserOfflineEvent(context.getUserId(), connection));
            logger.info("clean disconnected local route, userId: {}, route: {}", context.getUserId(), router);
        } else {
            logger.info("clean disconnected local route, not clean, userId: {}, route: {}", context.getUserId(), router);
        }
    }
}
