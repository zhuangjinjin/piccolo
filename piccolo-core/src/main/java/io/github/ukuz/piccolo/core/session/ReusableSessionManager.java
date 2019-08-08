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
package io.github.ukuz.piccolo.core.session;

import io.github.ukuz.piccolo.api.PiccoloContext;
import io.github.ukuz.piccolo.api.connection.SessionContext;
import io.github.ukuz.piccolo.common.cache.CacheKeys;
import io.github.ukuz.piccolo.common.properties.CoreProperties;
import io.github.ukuz.piccolo.common.security.MD5Utils;
import io.netty.util.internal.StringUtil;

/**
 * @author ukuz90
 */
public final class ReusableSessionManager {

    private PiccoloContext piccoloContext;
    private final int expireTime;

    public ReusableSessionManager(PiccoloContext piccoloContext) {
        this.piccoloContext = piccoloContext;
        this.expireTime = piccoloContext.getProperties(CoreProperties.class).getSessionExpireTime();
    }

    public void cacheSession(ReusableSession session) {
        String key = CacheKeys.getSessionKey(session.getSessionId());
        String encodeValue = ReusableSession.encode(session.getContext());
        piccoloContext.getCacheManager().set(key, encodeValue, expireTime);
    }

    public ReusableSession querySession(String sessionId) {
        String key = CacheKeys.getSessionKey(sessionId);
        String content = piccoloContext.getCacheManager().get(key, String.class);
        if (StringUtil.isNullOrEmpty(content)) {
            return null;
        }
        return ReusableSession.decode(content);
    }

    public ReusableSession genSession(SessionContext context) {
        long now = System.currentTimeMillis();
        ReusableSession session = new ReusableSession();
        session.setContext(context);
        session.setSessionId(MD5Utils.encrypt(context.getDeviceId() + now));
        session.setExpireTime(now + expireTime * 1000);
        return session;
    }
}
