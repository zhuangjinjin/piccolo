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
package io.github.ukuz.piccolo.core.router;

import io.github.ukuz.piccolo.api.connection.Connection;
import io.github.ukuz.piccolo.api.router.Router;

/**
 * @author ukuz90
 */
public class LocalRouter implements Router<Connection> {
    private Connection connection;

    public LocalRouter(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Connection getRouterValue() {
        return connection;
    }

    @Override
    public RouterType getRouterType() {
        return RouterType.LOCAL;
    }

    public Byte getClientType() {
        return connection.getSessionContext().getClientType();
    }

    @Override
    public String toString() {
        return "LocalRouter{" +
                "connection=" + connection +
                '}';
    }
}
