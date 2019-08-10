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
package io.github.ukuz.piccolo.client;

import io.github.ukuz.piccolo.api.config.Environment;
import io.github.ukuz.piccolo.api.exchange.handler.ChannelHandler;
import io.github.ukuz.piccolo.client.common.MultiMessageClientHandler;
import io.github.ukuz.piccolo.client.connect.ClientConfig;
import io.github.ukuz.piccolo.client.connect.ConnectClientHandler;

/**
 * @author ukuz90
 */
public final class ChannelHandlers {

    private ChannelHandlers() {}

    public static ChannelHandler newConnectClientHandler(Environment environment, ClientConfig clientConfig) {
        ConnectClientHandler handler = new ConnectClientHandler(environment, clientConfig);
        return new MultiMessageClientHandler(handler);
    }

    public static ChannelHandler newGatewayClientHandler(Environment environment) {
        return new MultiMessageClientHandler(null);
    }

}
