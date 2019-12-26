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
package io.github.ukuz.piccolo.cache.redis.connection.redisson;

import io.github.ukuz.piccolo.cache.redis.connection.RedisConnectionFactory;
import io.github.ukuz.piccolo.cache.redis.operator.HashOperator;
import io.github.ukuz.piccolo.cache.redis.operator.ListOperator;
import io.github.ukuz.piccolo.cache.redis.operator.ValueOperator;
import io.github.ukuz.piccolo.cache.redis.operator.ZSetOperator;
import io.github.ukuz.piccolo.cache.redis.properties.RedisProperties;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

/**
 * @author ukuz90
 */
public class RedissonConnectionFactory implements RedisConnectionFactory {

    private RedissonClient redissonClient;

    @Override
    public void init(RedisProperties redisProperties) {
        Config config = initConfig(redisProperties);

        redissonClient = Redisson.create(config);

    }

    @Override
    public ValueOperator getValueOperator(String key) {
        return null;
    }

    @Override
    public HashOperator getHashOperator(String key) {
        return null;
    }

    @Override
    public ListOperator getListOperator(String key) {
        return null;
    }

    @Override
    public ZSetOperator getZSetOperator(String key) {
        return null;
    }

    private Config initConfig(RedisProperties redisProperties) {
        Config config = new Config();

        if (redisProperties.isStandalone()) {

            initStandaloneConfig(config, redisProperties);

        } else if (redisProperties.isSentinel()) {

            initSentinelConfig(config, redisProperties);

        } else if (redisProperties.isCluster()) {

            initClusterConfig(config, redisProperties);

        }

        return config;
    }

    private void initStandaloneConfig(Config config, RedisProperties redisProperties) {
        config.useSingleServer().setAddress(redisProperties.getHost());
        config.useSingleServer().setPassword(redisProperties.getPassword());
    }

    private void initSentinelConfig(Config config, RedisProperties redisProperties) {
        config.useSentinelServers().addSentinelAddress(redisProperties.getSentinelMaster());
        config.useSentinelServers().setPassword(redisProperties.getPassword());
    }

    private void initClusterConfig(Config config, RedisProperties redisProperties) {
        config.useClusterServers().addNodeAddress(redisProperties.getHost());
        config.useClusterServers().setPassword(redisProperties.getPassword());
    }

    @Override
    public void destroy() {

    }

}
