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

import io.github.ukuz.piccolo.cache.redis.operator.ZSetOperator;
import redis.clients.jedis.JedisCommands;

import java.util.Set;

/**
 * @author ukuz90
 */
public class JedisZSetOperator extends JedisOperator implements ZSetOperator {

    private final String key;

    public JedisZSetOperator(String key, JedisCommands jedisCommands) {
        super(jedisCommands);
        this.key = key;
    }

    @Override
    public void zAdd(String field, double score) {
        run(commands -> commands.zadd(key, score, field));
    }

    @Override
    public long zCard() {
        return call(commands -> commands.zcard(key));
    }

    @Override
    public void zRem(String field) {
        run(commands -> commands.zrem(key, field));
    }

    @Override
    public Set<String> zRange(long start, long end) {
        return call(commands -> commands.zrange(key, start, end));
    }
}
