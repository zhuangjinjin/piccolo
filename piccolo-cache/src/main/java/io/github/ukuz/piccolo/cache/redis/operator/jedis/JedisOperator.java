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

import io.github.ukuz.piccolo.cache.CacheException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCommands;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author ukuz90
 */
public abstract class JedisOperator {

    private final JedisCommands commands;
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    public JedisOperator(JedisCommands commands) {
        this.commands = commands;
    }

    public void run(Consumer<JedisCommands> consumer) {
        LOGGER.info("redis run");
        try {
            consumer.accept(commands);
        } catch (Exception e) {
            LOGGER.warn("call failure, cause: {}", e);
            throw new CacheException("redis call failure", e);
        } finally {
            if (commands instanceof Jedis) {
                ((Jedis) commands).close();
            }
        }
    }

    public <T> T call(Function<JedisCommands, T> function) {
        LOGGER.info("redis call");
        try {
            return function.apply(commands);
        } catch (Exception e) {
            LOGGER.warn("call failure, cause: {}", e);
            throw new CacheException("redis call failure", e);
        } finally {
            if (commands instanceof Jedis) {
                ((Jedis) commands).close();
            }
        }
    }

}
