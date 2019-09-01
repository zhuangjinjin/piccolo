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

import io.github.ukuz.piccolo.api.connection.Connection;
import io.github.ukuz.piccolo.api.exchange.support.BaseMessage;
import io.github.ukuz.piccolo.api.service.discovery.ServiceListener;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author ukuz90
 */
public interface GatewayConnectionFactory extends ServiceListener {

    Connection getConnection(String hostAndPort);

//    Callback<Connection> getConnectionAsync(String hostAndPort);

    <T extends BaseMessage> boolean send(String hostAndPort, Function<Connection, T> creator, Consumer<T> sender);

    <T extends BaseMessage> boolean broadcast(Function<Connection, T> creator, Consumer<T> sender);

}
