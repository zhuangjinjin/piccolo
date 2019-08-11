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
import io.github.ukuz.piccolo.api.config.Environment;
import io.github.ukuz.piccolo.api.spi.SpiLoader;
import io.github.ukuz.piccolo.cache.redis.properties.RedisProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class RedisCacheManagerTest {

    private RedisCacheManager redisCacheManager;

    @Mock
    private PiccoloContext context;

    @BeforeEach
    void setUp() {
        Environment environment = SpiLoader.getLoader(Environment.class).getExtension();
        environment.scanAllProperties();
        environment.load("redis-test.properties");
        RedisProperties redis = environment.getProperties(RedisProperties.class);
        when(context.getProperties(RedisProperties.class)).thenReturn(redis);

        redisCacheManager = new RedisCacheManager();
        redisCacheManager.init(context);
    }

    @DisplayName("test_add")
    @Test
    void testGet() {
        redisCacheManager.set("piccolo", "test");
        assertEquals("test", redisCacheManager.get("piccolo"));
    }

}