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
package io.github.ukuz.piccolo.api.cache;

import io.github.ukuz.piccolo.api.spi.Spi;

import java.util.List;
import java.util.Map;

/**
 * @author ukuz90
 */
@Spi(primary = "redis")
public interface CacheManager {

    void init();

    void destroy();

    void del(String key);

    long hincrBy(String key, String field, long value);

    void set(String key, String value);

    void set(String key, String value, int expireTime);

    void set(String key, Object value, int expireTime);

    <T> T get(String key, Class<T> tClass);

    void hset(String key, String field, String value);

    void hset(String key, String field, Object value);

    <T> T hget(String key, String field, Class<T> tClass);

    void hdel(String key, String field);

    <T> Map<String, T> hgetAll(String key, Class<T> clazz);

    void zAdd(String key, String value);

    Long zCard(String key);

    void zRem(String key, String value);

    <T> List<T> zrange(String key, int start, int end, Class<T> clazz);

    void lpush(String key, String... value);

    <T> List<T> lrange(String key, int start, int end, Class<T> clazz);

}
