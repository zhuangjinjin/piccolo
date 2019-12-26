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
import io.github.ukuz.piccolo.api.spi.SpiLoader;
import io.github.ukuz.piccolo.cache.redis.connection.RedisConnectionFactory;
import io.github.ukuz.piccolo.cache.redis.properties.RedisProperties;
import io.github.ukuz.piccolo.common.json.Jsons;
import io.github.ukuz.piccolo.common.properties.CoreProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;


/**
 * @author ukuz90
 */
public class RedisCacheManager implements CacheManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisCacheManager.class);
    private RedisConnectionFactory factory;

    @Override
    public void init(PiccoloContext piccoloContext) {
        CoreProperties properties = piccoloContext.getProperties(CoreProperties.class);
        factory = SpiLoader.getLoader(RedisConnectionFactory.class).getExtension(properties.getCache());
        factory.init(piccoloContext.getProperties(RedisProperties.class));
    }

    @Override
    public void destroy() {
        factory.destroy();
    }

    @Override
    public void del(String key) {
        factory.getValueOperator(key).del();
    }

    @Override
    public long hincrBy(String key, String field, long value) {
        return factory.getHashOperator(key).incr(field, value);
    }

    @Override
    public void set(String key, String value) {
        factory.getValueOperator(key).set(value);
    }

    @Override
    public void set(String key, String value, int expireTime) {
        factory.getValueOperator(key).setEx(value, expireTime);
    }

    @Override
    public void set(String key, Object value, int expireTime) {
        factory.getValueOperator(key).setEx(Jsons.toJson(value), expireTime);
    }

    @Override
    public String get(String key) {
        return (String) factory.getValueOperator(key).get();
    }

    @Override
    public <T> T get(String key, Class<T> tClass) {
        return Jsons.fromJson((String) factory.getValueOperator(key).get(), tClass);
    }

    @Override
    public void hset(String key, String field, String value) {
        factory.getHashOperator(key).set(field, value);
    }

    @Override
    public void hset(String key, String field, Object value) {
        factory.getHashOperator(key).set(field, Jsons.toJson(value));
    }

    @Override
    public <T> T hget(String key, String field, Class<T> tClass) {
        return Jsons.fromJson((String) factory.getHashOperator(key).get(field), tClass);
    }

    @Override
    public void hdel(String key, String field) {
        factory.getHashOperator(key).del(field);
    }

    @Override
    public Map<String, String> hgetAll(String key) {
        return factory.getHashOperator(key).getAll();
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
        factory.getZSetOperator(key).zAdd(field, score);
    }

    @Override
    public Long zCard(String key) {
        return factory.getZSetOperator(key).zCard();
    }

    @Override
    public void zRem(String key, String field) {
        factory.getZSetOperator(key).zRem(field);
    }

    @Override
    public <T> List<T> zrange(String key, int start, int end, Class<T> clazz) {
        Set<String> tmpResult = factory.getZSetOperator(key).zRange(start, end);
        return tmpResult.stream()
                .map(v -> Jsons.fromJson(v, clazz))
                .collect(Collectors.toList());
    }

    @Override
    public void lpush(String key, String... value) {
        factory.getListOperator(key).lPush(value);
    }

    @Override
    public <T> List<T> lrange(String key, int start, int end, Class<T> clazz) {
        List<String> tmpResult = factory.getListOperator(key).lRange(start, end);
        return tmpResult.stream()
                .map(v -> Jsons.fromJson(v, clazz))
                .collect(Collectors.toList());
    }
}
