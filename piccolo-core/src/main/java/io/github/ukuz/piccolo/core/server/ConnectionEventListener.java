/*
 * Copyright 2020 ukuz90
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
package io.github.ukuz.piccolo.core.server;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import io.github.ukuz.piccolo.api.common.utils.StringUtils;
import io.github.ukuz.piccolo.api.connection.Connection;
import io.github.ukuz.piccolo.api.connection.SessionContext;
import io.github.ukuz.piccolo.api.event.ConnectionCloseEvent;
import io.github.ukuz.piccolo.api.event.UserOfflineEvent;
import io.github.ukuz.piccolo.common.event.EventBus;
import io.github.ukuz.piccolo.common.event.EventObservable;
import io.github.ukuz.piccolo.common.properties.NetProperties;

/**
 * @author ukuz90
 */
public class ConnectionEventListener extends EventObservable {

    private NetProperties netProperties;

    public ConnectionEventListener(NetProperties netProperties) {
        this.netProperties = netProperties;
    }

    @Subscribe
    @AllowConcurrentEvents
    public void on(ConnectionCloseEvent event) {
        if (!netProperties.isUserOfflineOnConnectionClose()) {
            return;
        }
        Connection connection = event.getConnection();
        SessionContext sessionContext = connection.getSessionContext();
        if (sessionContext != null && StringUtils.hasText(sessionContext.getUserId())) {
            EventBus.post(new UserOfflineEvent(sessionContext.getUserId(), connection));
        }
    }
}
