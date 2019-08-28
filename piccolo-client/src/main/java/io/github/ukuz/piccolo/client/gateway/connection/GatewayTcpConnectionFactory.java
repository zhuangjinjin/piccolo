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

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import com.google.common.net.HostAndPort;
import io.github.ukuz.piccolo.api.common.Holder;
import io.github.ukuz.piccolo.api.connection.Connection;
import io.github.ukuz.piccolo.api.event.ConnectionConnectEvent;
import io.github.ukuz.piccolo.api.exchange.support.BaseMessage;
import io.github.ukuz.piccolo.api.service.discovery.ServiceInstance;
import io.github.ukuz.piccolo.client.PiccoloClient;
import io.github.ukuz.piccolo.client.gateway.GatewayClient;
import io.github.ukuz.piccolo.common.ServiceNames;
import io.github.ukuz.piccolo.common.event.EventBus;
import io.netty.channel.ChannelFuture;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author ukuz90
 */
public class GatewayTcpConnectionFactory implements GatewayConnectionFactory {

    private final Logger logger = LoggerFactory.getLogger(GatewayConnectionFactory.class);

    private final AttributeKey<String> attrKey = AttributeKey.valueOf("host_port");
    private PiccoloClient piccoloClient;
    private GatewayClient gatewayClient;
    private int gatewayClientNum = 1;

    private ConcurrentMap<String, Holder<List<Connection>>> connections = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public GatewayTcpConnectionFactory(PiccoloClient piccoloClient) {
        this.piccoloClient = piccoloClient;

        this.gatewayClient = new GatewayClient(piccoloClient);
        this.gatewayClient.start();
        EventBus.register(this);
        piccoloClient.getServiceDiscovery().subscribe(ServiceNames.S_GATEWAY, this);
        piccoloClient.getServiceDiscovery().lookup(ServiceNames.S_GATEWAY).forEach(si -> this.syncAddConnection((ServiceInstance) si));
    }

    private void asyncAddConnection(ServiceInstance serviceInstance) {
        for (int i = 0; i < gatewayClientNum; i++) {
            addConnection(serviceInstance.getHost(), serviceInstance.getPort(), false);
        }
    }

    private void syncAddConnection(ServiceInstance serviceInstance) {
        for (int i = 0; i < gatewayClientNum; i++) {
            addConnection(serviceInstance.getHost(), serviceInstance.getPort(), true);
        }
    }

    private void addConnection(String host, int port, boolean sync) {
        ChannelFuture future = gatewayClient.connect(host, port);
        future.channel().attr(attrKey).set(getHostAndPort(host, port));
        future.addListener(f -> {
            if (f.isSuccess()) {
                logger.info("add connection success, host: {} port: {}", host, port);
            } else {
                logger.error("add connection failure, host: {} port: {} cause: {}", host, port, f.cause());
            }
        });
        if (sync) {
            future.awaitUninterruptibly();
        }
    }

    private String getHostAndPort(String host, int port) {
        return host + ":" + port;
    }


    @Override
    public Connection getConnection(String hostAndPort) {
        List<Connection> list = get(hostAndPort);
        if (list.isEmpty()) {
            synchronized (list) {
                //TODO zk补偿一次
                if (list.isEmpty()) {
                    return null;
                }
            }
        }
        int length = list.size();
        Connection connection;
        if (length == 1) {
            connection = list.get(0);
        } else {
            connection = list.get((int) (Math.random() * length % length));
        }
        if (connection.isConnected()) {
            return connection;
        }
        reconnect(connection, hostAndPort, length <= 1);
        return getConnection(hostAndPort);
    }

    private void reconnect(Connection connection, String hostAndPort, boolean sync) {
        HostAndPort h_p = HostAndPort.fromString(hostAndPort);
        get(hostAndPort).remove(connection);
        connection.close();
        addConnection(h_p.getHost(), h_p.getPort(), sync);
    }

    @Override
    public <T extends BaseMessage> boolean send(String hostAndPort, Function<Connection, T> creator, Consumer<T> sender) {
        Connection connection = getConnection(hostAndPort);

        return false;
    }

    @Override
    public <T extends BaseMessage> boolean broadcast(Function<Connection, T> creator, Consumer<T> sender) {
        return false;
    }

    @Override
    public void onServiceAdded(ServiceInstance serviceInstance) {
        asyncAddConnection(serviceInstance);
    }

    @Override
    public void onServiceUpdated(ServiceInstance serviceInstance) {
        removeClient(serviceInstance);
        asyncAddConnection(serviceInstance);
    }

    @Override
    public void onServiceDeleted(ServiceInstance serviceInstance) {
        removeClient(serviceInstance);
    }

    private void removeClient(ServiceInstance serviceInstance) {
        Holder<List<Connection>> holder = connections.remove(serviceInstance.getHostAndPort());
        if (holder != null) {
            if (holder.getValue() != null) {
                holder.getValue().forEach(Connection::close);
            }
        }
    }

    @Subscribe
    @AllowConcurrentEvents
    void on(ConnectionConnectEvent event) {
        Connection connection = event.getConnection();
        String hostAndPort = connection.getChannel().attr(attrKey).get();
        if (hostAndPort == null) {
            InetSocketAddress remoteAddress = (InetSocketAddress) connection.getChannel().remoteAddress();
            hostAndPort = getHostAndPort(remoteAddress.getAddress().getHostAddress(), remoteAddress.getPort());
        }
        get(hostAndPort).add(connection);
    }

    private List<Connection> get(String hostAndPort) {
        Holder<List<Connection>> holder = connections.computeIfAbsent(hostAndPort, key -> new Holder<>());
        if (holder.getValue() == null) {
            synchronized (holder) {
                if (holder.getValue() == null) {
                    holder.setValue(new ArrayList<>(gatewayClientNum));
                }
            }
        }
        return holder.getValue();
    }
}
