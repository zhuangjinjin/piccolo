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
package io.github.ukuz.piccolo.cache.redis.operator.jedis;

import io.github.ukuz.piccolo.cache.redis.operator.HashOperator;
import redis.clients.jedis.JedisCommands;

import java.util.Map;

/**
 * @author ukuz90
 */
public class JedisHashOperator<T> extends JedisOperator implements HashOperator<T> {

    private final String key;

    public JedisHashOperator(String key, JedisCommands jedisCommands) {
        super(jedisCommands);
        this.key = key;
    }

    @Override
    public long incr(String field, long value) {
        return call(commands -> commands.hincrBy(key, field, value));
    }

    @Override
    public void set(String field, T value) {
        run(commands -> commands.hset(key, field, value.toString()));
    }

    @Override
    public T get(String field) {
        return (T) call(commands -> commands.hget(key, field));
    }

    @Override
    public void del(String field) {
        run(commands -> commands.hdel(key, field));
    }

    @Override
    public Map getAll() {
        return call(commands -> commands.hgetAll(key));
    }
}
