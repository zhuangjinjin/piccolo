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
package io.github.ukuz.piccolo.common.router;

import io.github.ukuz.piccolo.api.common.utils.StringUtils;

/**
 * @author ukuz90
 */
public interface KickRemoteMsg {

    String getUserId();

    String getDeviceId();

    String getConnId();

    byte getClientType();

    String getTargetAddress();

    int getTargetPort();

    default boolean isTargetMatch(String host, int port) {
        return port == getTargetPort() && StringUtils.equals(getTargetAddress(), host);
    }

}
