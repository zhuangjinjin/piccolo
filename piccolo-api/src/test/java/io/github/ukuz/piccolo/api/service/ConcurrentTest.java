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
package io.github.ukuz.piccolo.api.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.ResourceLock;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.parallel.Resources.*;
import static org.junit.jupiter.api.parallel.ResourceAccessMode.*;

import java.util.Properties;

/**
 * @author ukuz90
 */
@Execution(ExecutionMode.CONCURRENT)
public class ConcurrentTest {

    Properties backup;

    @BeforeEach
    void setUp() {
        backup = new Properties();
        backup.putAll(System.getProperties());
    }

    @AfterEach
    void restore() {
//        backup.clear();
        System.setProperties(backup);
    }

    @Test
    @RepeatedTest(2)
    @ResourceLock(value = SYSTEM_PROPERTIES, mode = READ)
    void testCustomPropertyIfNotSetByDefault() {
        assertNull(backup.getProperty("my.prop"));
    }

    @Test
    @ResourceLock(value = SYSTEM_PROPERTIES, mode = READ_WRITE)
    void testSetCustomPropertyToApple() {
        backup.setProperty("my.prop", "apple");
        assertEquals("apple", backup.getProperty("my.prop"));
    }

    @Test
    @ResourceLock(value = SYSTEM_PROPERTIES, mode = READ_WRITE)
    void testSetCustomPropertyToBanana() {
        backup.setProperty("my.prop", "banana");
        assertEquals("banana", backup.getProperty("my.prop"));
    }

}
