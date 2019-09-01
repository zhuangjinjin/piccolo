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
package io.github.ukuz.piccolo.client.gateway.connection;

import com.google.common.net.HostAndPort;
import io.github.ukuz.piccolo.api.connection.Connection;
import io.github.ukuz.piccolo.client.gateway.GatewayClient;
import io.netty.channel.ChannelFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author ukuz90
 */
public class ConnectionList {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionList.class);
    private final int capacity;
    private final CopyOnWriteArrayList<Connection> connections;
    private final GatewayClient client;
    private final AtomicInteger connectNum = new AtomicInteger();
    private final String hostAndPort;

    public ConnectionList(String hostAndPort, int capacity, GatewayClient client) {
        this.capacity = capacity;
        this.client = client;
        this.hostAndPort = hostAndPort;
        this.connections = new CopyOnWriteArrayList<>();
    }

    public void syncAddConnection() {
        for (int i = 0; i < capacity; i++) {
            addConnection(hostAndPort, true);
        }
    }

    public void asyncAddConnection() {
        for (int i = 0; i < capacity; i++) {
            addConnection(hostAndPort, false);
        }
    }

    void addConnection(String hostAndPort, boolean sync) {
        int connNum = connectNum.get();
        if (connNum >= capacity) {
            return;
        }
        if (connectNum.compareAndSet(connNum, connNum + 1)) {
            HostAndPort hap = HostAndPort.fromString(hostAndPort);
            ChannelFuture channelFuture = client.connect(hap.getHost(), hap.getPort());
            channelFuture.addListener(f -> {
                if (f.isSuccess()) {
                    LOGGER.info("add connection success, hostAndPort:{} sync: {}", hostAndPort, sync);
                } else {
                    connectNum.decrementAndGet();
                    LOGGER.error("add connection failure, hostAndPort:{} sync: {} cause: {}", hostAndPort, sync, f.cause());
                }
            });
            if (sync) {
                channelFuture.awaitUninterruptibly();
            }
        }
    }

    void reconnect(Connection connection, String hostAndPort, boolean sync) {
        if (connections.remove(connection)) {
            int connNum = connectNum.decrementAndGet();
            LOGGER.info("close connection, and now have {} connections", connNum);
            connection.close();
            addConnection(hostAndPort, sync);
        }
    }

    public void close() {
        connections.forEach(Connection::close);
    }

    public Connection getConnection() {
        int len = connections.size();
        if (len == 0) {
            return null;
        }
        Connection connection;
        if (len == 1) {
            connection = connections.get(0);
        } else {
            connection = connections.get((int) (Math.random() * len % len));
        }
        if (connection.isConnected()) {
            return connection;
        }
        reconnect(connection, hostAndPort, connections.size() <= 1);
        return getConnection();
    }

    public void addConnection(Connection connection) {
        connections.add(connection);
    }
 }
