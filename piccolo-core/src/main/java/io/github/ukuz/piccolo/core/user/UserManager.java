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
package io.github.ukuz.piccolo.core.user;

import io.github.ukuz.piccolo.common.cache.CacheKeys;
import io.github.ukuz.piccolo.common.properties.NetProperties;
import io.github.ukuz.piccolo.common.router.RemoteRouter;
import io.github.ukuz.piccolo.core.PiccoloServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;

/**
 * @author ukuz90
 */
public class UserManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserManager.class);
    private final String onlineUserListKey;
    private PiccoloServer piccoloContext;

    public UserManager(PiccoloServer piccoloServer) {
        this.piccoloContext = piccoloServer;
        NetProperties net = piccoloContext.getProperties(NetProperties.class);
        this.onlineUserListKey = CacheKeys.getOnlineUserListKey(net.getPublicIp());
    }

    public void kickUser(String userId, byte clientType) {
        Set<RemoteRouter> remoteRouters = piccoloContext.getRouterCenter().lookupRemote(userId);
        remoteRouters
                .stream()
                .filter(remoteRouter -> (clientType == -1 || clientType == remoteRouter.getClientType()))
                .forEach(remoteRouter -> {

                });
    }

    public void clearOnlineList() {
        piccoloContext.getCacheManager().del(onlineUserListKey);
    }

    public void addToOnlineList(String userId) {
        piccoloContext.getCacheManager().zAdd(onlineUserListKey, userId, 0);
        LOGGER.info("user online, userId: {}", userId);
    }

    public void removeFromOnlineList(String userId) {
        piccoloContext.getCacheManager().zRem(onlineUserListKey, userId);
        LOGGER.info("user offline, userId: {}", userId);
    }

    public long getOnlineUserNum() {
        Long val = piccoloContext.getCacheManager().zCard(onlineUserListKey);
        return val == null ? 0 : val;
    }

    public long getOnlineUserNum(String publicIp) {
        String key = CacheKeys.getOnlineUserListKey(publicIp);
        Long val = piccoloContext.getCacheManager().zCard(key);
        return val == null ? 0 : val;
    }

    public List<String> getOnlineUserList(String publicIp, int start, int end) {
        String key = CacheKeys.getOnlineUserListKey(publicIp);
        return piccoloContext.getCacheManager().zrange(key, start, end, String.class);
    }

}
