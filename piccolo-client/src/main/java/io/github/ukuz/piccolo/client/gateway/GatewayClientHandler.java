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
package io.github.ukuz.piccolo.client.gateway;

import io.github.ukuz.piccolo.api.connection.Connection;
import io.github.ukuz.piccolo.api.exchange.ExchangeException;
import io.github.ukuz.piccolo.api.exchange.handler.ChannelHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ukuz90
 */
public class GatewayClientHandler implements ChannelHandler {

    private final Logger logger = LoggerFactory.getLogger(GatewayClientHandler.class);

    @Override
    public void connected(Connection connection) throws ExchangeException {

    }

    @Override
    public void disconnected(Connection connection) throws ExchangeException {

    }

    @Override
    public void sent(Connection connection, Object message) throws ExchangeException {

    }

    @Override
    public void received(Connection connection, Object message) throws ExchangeException {

    }

    @Override
    public void caught(Connection connection, Throwable exception) throws ExchangeException {

    }
}
