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

package io.github.ukuz.piccolo.config.properties;

import io.github.ukuz.piccolo.api.config.Environment;
import io.github.ukuz.piccolo.api.spi.SpiLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PropertiesEnvironmentTest {

    private Environment environment;

    @BeforeEach
    void setUp() {
        environment = SpiLoader.getLoader(Environment.class).getExtension();
        environment.scanAllProperties();
//        environment.load();
    }

    @DisplayName("test_getProperties")
    @Test
    void testGetProperties() {
//        RedisProperties redisProperties = environment.getProperties(RedisProperties.class);
//        assertEquals(20, redisProperties.getMaxConnNum());
//        assertEquals("standalone", redisProperties.getMode());
//        assertTrue(redisProperties.isEnabled());
//        assertEquals(10000L, redisProperties.getTimeBetweenEvictionRunsMillis());
//        assertEquals(100, redisProperties.getProp1());
//        assertEquals(1.1, redisProperties.getProp3());
//        assertEquals(1, redisProperties.getProp4());
    }
}