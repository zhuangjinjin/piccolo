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
package io.github.ukuz.piccolo.cache.redis.connection.jedis;

import io.github.ukuz.piccolo.cache.redis.RedisNode;
import io.github.ukuz.piccolo.cache.redis.connection.RedisConnectionFactory;
import io.github.ukuz.piccolo.cache.redis.operator.HashOperator;
import io.github.ukuz.piccolo.cache.redis.operator.ListOperator;
import io.github.ukuz.piccolo.cache.redis.operator.ValueOperator;
import io.github.ukuz.piccolo.cache.redis.operator.ZSetOperator;
import io.github.ukuz.piccolo.cache.redis.operator.jedis.JedisHashOperator;
import io.github.ukuz.piccolo.cache.redis.operator.jedis.JedisListOperator;
import io.github.ukuz.piccolo.cache.redis.operator.jedis.JedisValueOperator;
import io.github.ukuz.piccolo.cache.redis.operator.jedis.JedisZSetOperator;
import io.github.ukuz.piccolo.cache.redis.properties.RedisProperties;
import io.netty.util.internal.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.*;
import redis.clients.util.Pool;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author ukuz90
 */
public class JedisConnectionFactory implements RedisConnectionFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(JedisConnectionFactory.class);

    private RedisProperties properties;
    private List<RedisNode> redisServers;

    private int timeout = Protocol.DEFAULT_TIMEOUT;

    private JedisShardInfo shardInfo;
    private JedisCluster cluster;
    private Pool<Jedis> pool;

    private int database = Protocol.DEFAULT_DATABASE;

    @Override
    public void init(RedisProperties redisProperties) {
        this.properties = redisProperties;
        redisServers = RedisNode.from(properties.getHost());

        shardInfo = createShareInfo();
        Optional.ofNullable(properties.getDatabase()).ifPresent(v -> database=v);

        if (properties.isCluster()) {
            cluster = createCluster();
        } else {
            pool = createPool();
        }
    }

    @Override
    public ValueOperator getValueOperator(String key) {
        return new JedisValueOperator(key, getCommands());
    }

    @Override
    public HashOperator getHashOperator(String key) {
        return new JedisHashOperator(key, getCommands());
    }

    @Override
    public ListOperator getListOperator(String key) {
        return new JedisListOperator(key, getCommands());
    }

    @Override
    public ZSetOperator getZSetOperator(String key) {
        return new JedisZSetOperator(key, getCommands());
    }

    private JedisCommands getCommands() {
        if (properties.isCluster()) {
            return getJedisClusterConnection();
        } else {
            return getJedisConnection();
        }
    }

    private JedisShardInfo createShareInfo() {
        JedisShardInfo shardInfo = new JedisShardInfo(redisServers.get(0).getHost(), redisServers.get(0).getPort());
        if (!StringUtil.isNullOrEmpty(properties.getPassword())) {
            shardInfo.setPassword(properties.getPassword());
        }
        if (timeout > 0) {
            shardInfo.setConnectionTimeout(timeout);
        }
        return shardInfo;
    }

    private JedisCluster createCluster() {
        Set<HostAndPort> hostAndPorts = redisServers
                .stream()
                .map(node -> new HostAndPort(node.getHost(), node.getPort()))
                .collect(Collectors.toSet());
        int maxAttempts = 5;
        return new JedisCluster(hostAndPorts, timeout, maxAttempts, properties.getPoolConfig().apply());
    }

    private Pool<Jedis> createPool() {
        if (properties.isSentinel()) {
            return createRedisSentinelPool();
        }
        return createRedisPool();
    }

    private Pool<Jedis> createRedisPool() {

        return new JedisPool(properties.getPoolConfig().apply(),
                shardInfo.getHost(),
                shardInfo.getPort(),
                shardInfo.getSoTimeout(),
                shardInfo.getPassword(),
                properties.getDatabase() >= 0 ? properties.getDatabase() : Protocol.DEFAULT_DATABASE);
    }

    private Pool<Jedis> createRedisSentinelPool() {
        Set<String> sentinels = redisServers
                .stream()
                .map(RedisNode::getHostAndPort)
                .collect(Collectors.toSet());

        return new JedisSentinelPool(properties.getSentinelMaster(), sentinels, properties.getPoolConfig().apply());
    }

    public JedisCluster getJedisClusterConnection() {
        return cluster;
    }

    public Jedis getJedisConnection() {
        Jedis jedis = fetchJedisConnector();
        if (jedis != null) {
            jedis.select(database);
        }
        return jedis;
    }

    private Jedis fetchJedisConnector() {
        try {
            if (pool != null) {
                return pool.getResource();
            }
            Jedis jedis = new Jedis(shardInfo);
            jedis.connect();
            return jedis;
        } catch (Exception e) {
            LOGGER.warn("Can not get redis connection, cause: {}", e);
            throw new RuntimeException("Can not get redis connection",
                    e);
        }
    }

    @Override
    public void destroy() {
        if (this.pool != null) {
            try {
                this.pool.destroy();
            } catch (Exception e) {
                LOGGER.warn("pool destroy failure, cause: {}", e);
            }
            this.pool = null;
        }
        if (this.cluster != null) {
            try {
                this.cluster.close();
            } catch (IOException e) {
                LOGGER.warn("cluster destroy failure, cause: {}", e);
            }
            this.cluster = null;
        }
    }

    public void setDatabase(int database) {
        this.database = database;
    }

    public boolean isCluster() {
        return properties.isCluster();
    }

    public boolean isSentinel() {
        return properties.isSentinel();
    }
}
