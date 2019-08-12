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
package io.github.ukuz.piccolo.cache.redis.properties;

import io.github.ukuz.piccolo.api.config.ConfigurationProperties;
import io.github.ukuz.piccolo.api.config.Properties;
import io.netty.util.internal.StringUtil;
import lombok.Data;
import redis.clients.jedis.JedisPoolConfig;

/**
 * @author ukuz90
 */
@ConfigurationProperties(prefix = "piccolo.redis")
@Data
public class RedisProperties implements Properties {

    public static final String MODE_STANDALONE = "standalone";
    public static final String MODE_SENTINEL = "sentinel";
    public static final String MODE_CLUSTER = "cluster";

    /**
     * host (eg: ip1:port1,ip2:port2,...)
     */
    private String host;
    /**
     * mode (eg: standalone / sentinel / cluster)
     */
    private String mode;
    /**
     * >= 0 (default 0)
     */
    private int database;
    private String sentinelMaster;
    private String password;

    private RedisPoolNestedConfig poolConfig;

    public boolean isSentinel() {
        return MODE_SENTINEL.equals(mode) && !StringUtil.isNullOrEmpty(sentinelMaster);
    }

    public boolean isCluster() {
        return MODE_CLUSTER.equals(mode);
    }

    /**
     * @see org.apache.commons.pool2.impl.BaseObjectPoolConfig
     */
    @Data
    public class RedisPoolNestedConfig implements Properties {

        private boolean testWhileIdle;
        private boolean testOnBorrow;
        private long minEvictableIdleTimeMillis;
        private long timeBetweenEvictionRunsMillis;
        private int numTestsPerEvictionRun;

        public JedisPoolConfig apply() {
            JedisPoolConfig config = new JedisPoolConfig();
            config.setTestWhileIdle(testWhileIdle);
            config.setTestOnBorrow(testOnBorrow);
            config.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
            config.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
            config.setNumTestsPerEvictionRun(numTestsPerEvictionRun);
            return config;
        }

    }

}
