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
import io.netty.channel.ChannelId;
import io.netty.util.internal.PlatformDependent;

import java.util.concurrent.ConcurrentMap;

/**
 * @author ukuz90
 */
public class NettyConnectionManager implements ConnectionManager {

    private final ConcurrentMap<ChannelId, Connection> CONNECTION_MAP = PlatformDependent.newConcurrentHashMap();

    @Override
    public Connection getConnection(Channel channel) {
        return CONNECTION_MAP.get(channel.id());
    }

    @Override
    public void add(Connection connection) {
        CONNECTION_MAP.putIfAbsent(connection.getChannel().id(), connection);
    }

    @Override
    public Connection removeConnection(Channel channel) {
        return CONNECTION_MAP.remove(channel.id());
    }

    @Override
    public int getConnectionNum() {
        return CONNECTION_MAP.size();
    }

    @Override
    public void init() {

    }

    @Override
    public void destroy() {
        CONNECTION_MAP.values().forEach(Connection::close);
        CONNECTION_MAP.clear();
    }
}
