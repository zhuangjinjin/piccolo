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
package io.github.ukuz.piccolo.api.common;

import java.util.ArrayList;
import java.util.List;

import io.github.ukuz.piccolo.api.annotation.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DefaultOrderComparatorTest {

    private DefaultOrderComparator<Class> comparator;

    @BeforeEach
    void setUp() {
        comparator = new DefaultOrderComparator<>();
    }

    @DisplayName("test_compare_Asc")
    @Test
    void testCompareAsc() {
        List<Class> list = new ArrayList<>();
        list.add(Order2.class);
        list.add(Order1.class);

        list = comparator.compare(list);
        assertEquals(Order1.class, list.get(0));
        assertEquals(Order2.class, list.get(1));
    }

    @DisplayName("test_compare_Desc")
    @Test
    void testCompareDesc() {
        List<Class> list = new ArrayList<>();
        list.add(Order1.class);
        list.add(Order2.class);

        list = comparator.compare(list, false);
        assertEquals(Order2.class, list.get(0));
        assertEquals(Order1.class, list.get(1));
    }

    @DisplayName("test_compare_NotOrder")
    @Test
    void testCompareNotOrder() {
        List<Class> list = new ArrayList<>();
        list.add(Order1.class);
        list.add(NoOrder1.class);

        try {
            comparator.compare(list);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Class io.github.ukuz.piccolo.api.common.DefaultOrderComparatorTest$NoOrder1 doesn't annotated with @Order", e.getMessage());
        }

    }

    @Order(value = 1)
    private static class Order1 {

    }

    @Order(value = 2)
    private static class Order2 {

    }

    private static class NoOrder1 {

    }

}