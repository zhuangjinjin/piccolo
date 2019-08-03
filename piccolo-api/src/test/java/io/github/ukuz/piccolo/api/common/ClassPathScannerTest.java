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

import io.github.ukuz.piccolo.api.annotation.AnnotationTypeFilter;
import io.github.ukuz.piccolo.api.common.sub.ClassWithInclude;
import io.github.ukuz.piccolo.api.common.sub.Exclude;
import io.github.ukuz.piccolo.api.common.sub.Include;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ClassPathScannerTest {

    @DisplayName("test_ScanAmbiguity")
    @Test
    void testScanAmbiguity() {
        ClassPathScanner scanner = new ClassPathScanner();
        scanner.addIncludeFilter(new AnnotationTypeFilter(Include.class));
        try {
            scanner.addExcludeFilter(new AnnotationTypeFilter(Include.class));
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("AnnotationTypeFilter{annotationType=interface io.github.ukuz.piccolo.api.common.sub.Include} was already in include filter set", e.getMessage());
        }
    }

    @DisplayName("test_Scan")
    @Test
    void testScan() throws URISyntaxException, ClassNotFoundException {
        ClassPathScanner scanner = new ClassPathScanner();
        scanner.addIncludeFilter(new AnnotationTypeFilter(Include.class));
        scanner.addExcludeFilter(new AnnotationTypeFilter(Exclude.class));
        Set<Class> candidate = scanner.scan(new String[]{"io.github.ukuz.piccolo.api.common"});
        assertEquals(1, candidate.size());
        assertEquals(ClassWithInclude.class, candidate.iterator().next());
    }
}