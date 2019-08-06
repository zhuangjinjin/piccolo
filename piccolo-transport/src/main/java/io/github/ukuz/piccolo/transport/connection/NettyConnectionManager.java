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
package io.github.ukuz.piccolo.transport.connection;

import io.github.ukuz.piccolo.api.connection.Connection;
import io.github.ukuz.piccolo.api.connection.ConnectionManager;
import io.netty.channel.Channel;

/**
 * @author ukuz90
 */
public class NettyConnectionManager implements ConnectionManager {
    @Override
    public Connection getConnection(Channel channel) {
        return null;
    }

    @Override
    public void add(Connection connection) {

    }

    @Override
    public Connection removeAndClose(Channel channel) {
        return null;
    }

    @Override
    public int getConnectionNum() {
        return 0;
    }

    @Override
    public void init() {

    }

    @Override
    public void destroy() {

    }
}
