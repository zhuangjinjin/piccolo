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
package io.github.ukuz.piccolo.cache.redis.manager;

import io.github.ukuz.piccolo.api.PiccoloContext;
import io.github.ukuz.piccolo.api.cache.CacheManager;
import io.github.ukuz.piccolo.cache.CacheException;
import io.github.ukuz.piccolo.cache.redis.connection.RedisConnectionFactory;
import io.github.ukuz.piccolo.cache.redis.properties.RedisProperties;
import io.github.ukuz.piccolo.common.json.Jsons;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisCommands;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * @author ukuz90
 */
public class RedisCacheManager implements CacheManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisCacheManager.class);
    private RedisConnectionFactory factory;

    @Override
    public void init(PiccoloContext piccoloContext) {
        factory = new RedisConnectionFactory();
        factory.init(piccoloContext.getProperties(RedisProperties.class));
    }

    @Override
    public void destroy() {
        factory.destroy();
    }

    public void run(Consumer<JedisCommands> consumer) {
        LOGGER.info("redis run");
        if (factory.isCluster()) {
            try {
                consumer.accept(factory.getJedisClusterConnection());
            } catch (Exception e) {
                LOGGER.warn("call failure, cause: {}", e);
                throw new CacheException("redis call failure", e);
            }
        } else {
            try {
                consumer.accept(factory.getJedisConnection());
            } catch (Exception e) {
                LOGGER.warn("call failure, cause: {}", e);
                throw new CacheException("redis call failure", e);
            }
        }
    }

    public <R> R call(Function<JedisCommands, R> function) {
        LOGGER.info("redis call");
        R result;
        if (factory.isCluster()) {
            try {
                result = function.apply(factory.getJedisClusterConnection());
            } catch (Exception e) {
                LOGGER.warn("call failure, cause: {}", e);
                throw new CacheException("redis call failure", e);
            }
        } else {
            try {
                result = function.apply(factory.getJedisConnection());
            } catch (Exception e) {
                LOGGER.warn("call failure, cause: {}", e);
                throw new CacheException("redis call failure", e);
            }
        }
        return result;
    }

    @Override
    public void del(String key) {
        run(redis -> redis.del(key));
    }

    @Override
    public long hincrBy(String key, String field, long value) {
        return call(redis -> redis.hincrBy(key, field, value));
    }

    @Override
    public void set(String key, String value) {
        run(redis -> redis.set(key, value));
    }

    @Override
    public void set(String key, String value, int expireTime) {
        run(redis -> redis.setex(key, expireTime, value));
    }

    @Override
    public void set(String key, Object value, int expireTime) {
        run(redis -> redis.setex(key, expireTime, Jsons.toJson(value)));
    }

    @Override
    public String get(String key) {
        return call(redis -> redis.get(key));
    }

    @Override
    public <T> T get(String key, Class<T> tClass) {
        return call(redis -> Jsons.fromJson(redis.get(key), tClass));
    }

    @Override
    public void hset(String key, String field, String value) {
        run(redis -> redis.hset(key, field, value));
    }

    @Override
    public void hset(String key, String field, Object value) {
        run(redis -> redis.hset(key, field, Jsons.toJson(value)));
    }

    @Override
    public <T> T hget(String key, String field, Class<T> tClass) {
        return call(redis -> Jsons.fromJson(redis.hget(key, field), tClass));
    }

    @Override
    public void hdel(String key, String field) {
        run(redis -> redis.hdel(key, field));
    }

    @Override
    public Map<String, String> hgetAll(String key) {
        return call(redis -> redis.hgetAll(key));
    }

    @Override
    public <T> Map<String, T> hgetAll(String key, Class<T> clazz) {
        Map<String, String> map = hgetAll(key);
        if (map.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, T> result = new HashMap<>(map.size());
        map.forEach((k, v) -> result.put(k, Jsons.fromJson(v, clazz)) );
        return result;
    }

    @Override
    public void zAdd(String key, String field, double score) {
        run(redis -> redis.zadd(key, score, field));
    }

    @Override
    public Long zCard(String key) {
        return call(redis -> redis.zcard(key));
    }

    @Override
    public void zRem(String key, String field) {
        run(redis -> redis.zrem(key, field));
    }

    @Override
    public <T> List<T> zrange(String key, int start, int end, Class<T> clazz) {
        Set<String> tmpResult = call(redis -> redis.zrange(key, start, end));
        return tmpResult.stream()
                .map(v -> Jsons.fromJson(v, clazz))
                .collect(Collectors.toList());
    }

    @Override
    public void lpush(String key, String... value) {
        run(redis -> redis.lpush(key, value));
    }

    @Override
    public <T> List<T> lrange(String key, int start, int end, Class<T> clazz) {
        List<String> tmpResult = call(redis -> redis.lrange(key, start, end));
        return tmpResult.stream()
                .map(v -> Jsons.fromJson(v, clazz))
                .collect(Collectors.toList());
    }
}
