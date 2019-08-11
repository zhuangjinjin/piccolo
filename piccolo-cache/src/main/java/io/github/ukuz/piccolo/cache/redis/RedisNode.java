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
package io.github.ukuz.piccolo.cache.redis;

import io.netty.util.internal.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author ukuz90
 */
public final class RedisNode {

    private String host;
    private int port;

    private static final int DEFAULT_PORT = 6379;

    public RedisNode(String host) {
        this.host = host;
        this.port = DEFAULT_PORT;
    }

    public RedisNode(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getHostAndPort() {
        return host + ":" + port;
    }

    public static List<RedisNode> from(String hostAndPort) {
        if (StringUtil.isNullOrEmpty(hostAndPort)) {
            throw new IllegalArgumentException("hostAndPort must not be null");
        }
        String[] arr = hostAndPort.replaceAll(" ", "").split("[,:]");
        if ((arr.length & 1) == 1) {
            throw new IllegalArgumentException("invalid param: " + hostAndPort);
        }
        List<RedisNode> list = new ArrayList<>(arr.length >> 1);
        for (int i = 0; i < arr.length; i+=2) {
            list.add(new RedisNode(arr[i], Integer.parseInt(arr[i + 1])));
        }
        return list;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RedisNode redisNode = (RedisNode) o;
        return port == redisNode.port &&
                Objects.equals(host, redisNode.host);
    }

    @Override
    public int hashCode() {
        int result = host.hashCode();
        return 31 * result + port;
    }

    @Override
    public String toString() {
        return "RedisNode{" +
                "host='" + host + '\'' +
                ", port=" + port +
                '}';
    }

}
