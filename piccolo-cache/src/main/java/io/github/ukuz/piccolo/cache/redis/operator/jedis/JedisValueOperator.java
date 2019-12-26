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

import io.github.ukuz.piccolo.cache.redis.operator.ValueOperator;
import redis.clients.jedis.JedisCommands;

import java.util.concurrent.TimeUnit;

/**
 * @author ukuz90
 */
public class JedisValueOperator extends JedisOperator implements ValueOperator<String> {

    private final String key;

    public JedisValueOperator(String key, JedisCommands jedisCommands) {
        super(jedisCommands);
        this.key = key;
    }

    @Override
    public String set(String val) {
        return call(commands -> commands.set(key, val));
    }

    @Override
    public long setNx(String val) {
        return call(commands -> commands.setnx(key, val));
    }

    @Override
    public String setEx(String val, int seconds) {
        return call(commands -> commands.setex(key, seconds, val));
    }

    @Override
    public String psetEx(String val, long millis) {
        return call(commands -> commands.psetex(key, millis, val));
    }

    @Override
    public String get() {
        return call(commands -> commands.get(key));
    }

    @Override
    public long incr() {
        return call(commands -> commands.incr(key));
    }

    @Override
    public long incrBy(long increment) {
        return call(commands -> commands.incrBy(key, increment));
    }

    @Override
    public long decr() {
        return call(commands -> commands.decr(key));
    }

    @Override
    public long decrBy(long decrement) {
        return call(commands -> commands.decrBy(key, decrement));
    }

    @Override
    public long del() {
        return call(commands -> commands.del(key));
    }
}
