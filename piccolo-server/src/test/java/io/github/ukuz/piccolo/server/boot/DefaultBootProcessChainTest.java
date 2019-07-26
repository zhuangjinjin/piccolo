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
package io.github.ukuz.piccolo.server.boot;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DefaultBootProcessChainTest {

    @DisplayName("test_addFirst")
    @Test
    void testAddFirst() {
        BootProcessChain processChain = new DefaultBootProcessChain();
        final List<Integer> list = new ArrayList<>(2);
        for (int i = 0; i < 2; i++) {
            final Integer tmp = i;
            processChain.addFirst(new BootJob() {
                @Override
                public void start() {
                    list.add(tmp);
                }
            });
        }
        processChain.start();
        assertEquals(Integer.valueOf(1), list.get(0));
        assertEquals(Integer.valueOf(0), list.get(1));
    }

    @DisplayName("test_addLast")
    @Test
    void testAddLast() {
        BootProcessChain processChain = new DefaultBootProcessChain();
        final List<Integer> list = new ArrayList<>(2);
        for (int i = 0; i < 2; i++) {
            final Integer tmp = i;
            processChain.addLast(new BootJob() {
                @Override
                public void start() {
                    list.add(tmp);
                }
            });
        }
        processChain.start();
        assertEquals(Integer.valueOf(0), list.get(0));
        assertEquals(Integer.valueOf(1), list.get(1));
    }

}