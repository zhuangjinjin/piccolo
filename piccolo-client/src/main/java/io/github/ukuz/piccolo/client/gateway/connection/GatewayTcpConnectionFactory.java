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
import io.github.ukuz.piccolo.common.loadbalance.RandomLoadbalancer;
import io.netty.channel.ChannelFuture;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author ukuz90
 */
public class GatewayTcpConnectionFactory implements GatewayConnectionFactory {

    private final Logger logger = LoggerFactory.getLogger(GatewayTcpConnectionFactory.class);

    private final AttributeKey<String> attrKey = AttributeKey.valueOf("host_port");
    private PiccoloClient piccoloClient;
    private GatewayClient gatewayClient;
    private int gatewayClientNum = 1;

    private ConcurrentMap<String, ConnectionList> connections = new ConcurrentHashMap<>();

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
        get(serviceInstance.getHostAndPort()).asyncAddConnection();
    }

    private void syncAddConnection(ServiceInstance serviceInstance) {
        logger.info("syncAddConnection");
        get(serviceInstance.getHostAndPort()).syncAddConnection();
    }

    private String getHostAndPort(String host, int port) {
        return host + ":" + port;
    }


    @Override
    public Connection getConnection(String hostAndPort) {
        Connection connection = get(hostAndPort).getConnection();
        if (connection == null) {
            synchronized (hostAndPort.intern()) {
                connection = get(hostAndPort).getConnection();
                if (connection == null) {
                    logger.warn("zk 补偿");
                    List<ServiceInstance> serviceInstances = piccoloClient.getServiceDiscovery().lookup(ServiceNames.S_GATEWAY);
                    serviceInstances.stream()
                            .filter(si -> si.getHostAndPort().equals(hostAndPort))
                            .forEach(this::syncAddConnection);

                    try {
                        TimeUnit.MILLISECONDS.sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    connection = get(hostAndPort).getConnection();
                }
            }
        }
        if (connection != null && connection.isConnected()) {
            logger.info("getConnection success, conn: {}", connection);
        } else {
            logger.error("getConnection failure, conn: {}", connection);
        }

        return connection;
    }

    @Override
    public <T extends BaseMessage> boolean send(String hostAndPort, Function<Connection, T> creator, Consumer<T> sender) {
//        Connection connection = getConnection(hostAndPort);

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
        ConnectionList connectionList = connections.remove(serviceInstance.getHostAndPort());
        if (connectionList != null) {
            connectionList.close();
        }
    }

    @Subscribe
    @AllowConcurrentEvents
    void on(ConnectionConnectEvent event) {
        Connection connection = event.getConnection();
        String hostAndPort = connection.getChannel().attr(attrKey).get();
        if (hostAndPort == null) {
            InetSocketAddress remoteAddress = (InetSocketAddress) connection.getChannel().remoteAddress();
            hostAndPort = getHostAndPort(remoteAddress.getHostName(), remoteAddress.getPort());
        }
        logger.warn("received ConnectionConnectEvent hostAndPort: {} conn: {}", hostAndPort, event.getConnection());
        get(hostAndPort).addConnection(connection);
    }

    private ConnectionList get(String hostAndPort) {
        return connections.computeIfAbsent(hostAndPort, key -> new ConnectionList(key, gatewayClientNum, gatewayClient));
    }

}
