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
package io.github.ukuz.piccolo.api.connection;

import io.netty.channel.Channel;

/**
 * @author ukuz90
 */
public interface ConnectionManager {

    /**
     * get connection by channel
     *
     * @param channel
     * @return
     */
    Connection getConnection(Channel channel);

    /**
     * add connection
     *
     * @param connection
     */
    void add(Connection connection);

    /**
     * remove connection
     *
     * @param channel
     * @return
     */
    Connection removeConnection(Channel channel);

    /**
     * get connection num
     *
     * @return
     */
    int getConnectionNum();

    void init();

    void destroy();

}
