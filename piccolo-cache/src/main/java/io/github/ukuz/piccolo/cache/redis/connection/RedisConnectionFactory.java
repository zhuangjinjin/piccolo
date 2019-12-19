package io.github.ukuz.piccolo.cache.redis.connection;

import io.github.ukuz.piccolo.cache.redis.properties.RedisProperties;
import redis.clients.jedis.Jedis;

public interface RedisConnectionFactory {
    void init(RedisProperties redisProperties);

    Jedis getJedisConnection();

    void destroy();

    void setDatabase(int database);

    boolean isCluster();

    boolean isSentinel();
}
