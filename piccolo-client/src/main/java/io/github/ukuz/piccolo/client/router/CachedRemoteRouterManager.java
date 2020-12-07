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
package io.github.ukuz.piccolo.client.router;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.github.ukuz.piccolo.api.cache.CacheManager;
import io.github.ukuz.piccolo.common.router.RemoteRouter;
import io.github.ukuz.piccolo.common.router.RemoteRouterManager;

import java.time.Duration;
import java.util.Set;

/**
 * @author ukuz90
 */
public class CachedRemoteRouterManager extends RemoteRouterManager {

    private Cache<String, Set<RemoteRouter>> cache = CacheBuilder.newBuilder()
            .expireAfterAccess(Duration.ofMinutes(5))
            .expireAfterWrite(Duration.ofMinutes(5))
            .build();

    public CachedRemoteRouterManager(CacheManager cacheManager) {
        super(cacheManager);
    }

    @Override
    public RemoteRouter lookup(String userId, byte clientType) {
        Set<RemoteRouter> cached = this.lookupAll(userId);
        for (RemoteRouter remoteRouter : cached) {
            if (remoteRouter.getRouterValue().getClientType() == clientType) {
                return remoteRouter;
            }
        }
        return null;
    }

    @Override
    public Set<RemoteRouter> lookupAll(String userId) {
        Set<RemoteRouter> cached = cache.getIfPresent(userId);
        if (cached != null) {
            return cached;
        }
        cached = super.lookupAll(userId);
        if (cached != null) {
            cache.put(userId, cached);
        }
        return cached;
    }

    /**
     * invalid userId's cache
     * @param userId
     */
    public void invalidate(String userId) {
        cache.invalidate(userId);
    }

}
