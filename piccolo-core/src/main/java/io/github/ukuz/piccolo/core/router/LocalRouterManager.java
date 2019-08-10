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

import io.github.ukuz.piccolo.api.router.RouterManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author ukuz90
 */
public class LocalRouterManager implements RouterManager<LocalRouter> {

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
}
