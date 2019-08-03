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
package io.github.ukuz.piccolo.api.spi;

import io.github.ukuz.piccolo.api.common.DefaultOrderComparator;
import io.github.ukuz.piccolo.api.common.OrderComparator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SpiLoaderTest {

    @DisplayName("test_getLoader_Null")
    @Test
    void testGetLoaderNull() {
        try {
            SpiLoader.getLoader(null);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Spi type must not be null", e.getMessage());
        }
    }

    @DisplayName("test_getLoader_NotInterface")
    @Test
    void testGetLoaderNotInterface() {
        try {
            SpiLoader.getLoader(SimpleOutput.class);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Spi type io.github.ukuz.piccolo.api.spi.SimpleOutput must an interface", e.getMessage());
        }
    }

    @DisplayName("test_getLoader_NotSpiAnnotation")
    @Test
    void testGetLoaderNotSpiAnnotation() {
        try {
            SpiLoader.getLoader(Output.class);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Spi type io.github.ukuz.piccolo.api.spi.Output must annotated with @Spi", e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    @DisplayName("test_getLoader")
    @Test
    void testGetLoader() {
        SpiLoader<Input> loader = SpiLoader.getLoader(Input.class);
        assertNotNull(loader);
    }

    @SuppressWarnings("unchecked")
    @DisplayName("test_getExtension_NotPrimaryKey")
    @Test
    void testGetExtensionNotPrimaryKey() {
        SpiLoader<Input> loader = SpiLoader.getLoader(Input.class);
        try {
            loader.getExtension();
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Can not found default extension", e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    @DisplayName("test_getExtension_PrimaryKeyNotMapping")
    @Test
    void testGetExtensionPrimaryKeyNotMapping() {
        SpiLoader<SpiPrimaryKeyNotMapping> loader = SpiLoader.getLoader(SpiPrimaryKeyNotMapping.class);
        try {
            loader.getExtension();
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Can not found default mapping class", e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    @DisplayName("test_getExtension")
    @Test
    void testGetExtensionWithPrimaryKey() {
        SpiLoader<OrderComparator> loader = SpiLoader.getLoader(OrderComparator.class);
        OrderComparator comparator = loader.getExtension();
        assertEquals(DefaultOrderComparator.class, comparator.getClass());
    }

    @SuppressWarnings("unchecked")
    @DisplayName("test_getExtensionWithOrder")
    @Test
    void testGetExtensionWithOrder() {
        SpiLoader<SpiWithOrder> loader = SpiLoader.getLoader(SpiWithOrder.class);
        SpiWithOrder spiWithOrder = loader.getExtension();
        assertEquals(SpiWithOrder.PrimarySpiWithOrder.class, spiWithOrder.getClass());
    }

    @DisplayName("test_getExtension_Null")
    @Test
    void testGetExtensionNull() {
        try {
            SpiLoader.getLoader(Input.class).getExtension("default");
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Can not found default mapping class", e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    @DisplayName("test_getExtension_WithKey")
    @Test
    void testGetExtensionWithKey() {
        SpiLoader<Input> loader = SpiLoader.getLoader(Input.class);
        Input input = loader.getExtension("hello");
        assertEquals("Hello, World!", input.read());
    }

}