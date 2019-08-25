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
package io.github.ukuz.piccolo.common.cache;

/**
 * @author ukuz90
 */
public final class CacheKeys {

    private static final String USER_PREFIX = "piccolo:ur:";

    private static final String SESSION_PREFIX = "piccolo:sk:";

    private static final String FAST_CONNECTION_DEVICE_PREFIX = "piccolo:fcd:";

    private static final String ONLINE_USER_LIST_KEY_PREFIX = "piccolo:oul:";

    public static String getSessionKey(String sessionId) {
        return SESSION_PREFIX + sessionId;
    }

    public static String getDeviceIdKey(String deviceId) {
        return FAST_CONNECTION_DEVICE_PREFIX + deviceId;
    }

    public static String getUserRouteKey(String userId) {
        return USER_PREFIX + userId;
    }

    public static String getOnlineUserListKey(String publicIP) {
        return ONLINE_USER_LIST_KEY_PREFIX + publicIP;
    }
}
