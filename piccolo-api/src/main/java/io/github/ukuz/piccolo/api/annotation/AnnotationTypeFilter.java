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
package io.github.ukuz.piccolo.api.annotation;

import java.util.Objects;

/**
 * @author ukuz90
 */
public class AnnotationTypeFilter {

    private final Class annotationType;

    public AnnotationTypeFilter(Class annotationType) {
        this.annotationType = annotationType;
    }

    public boolean match(Class targetClass) {
        return targetClass.isAnnotationPresent(annotationType);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(annotationType);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof AnnotationTypeFilter)) {
            return false;
        }
        return ((AnnotationTypeFilter)obj).annotationType == this.annotationType;
    }

    @Override
    public String toString() {
        return "AnnotationTypeFilter{" +
                "annotationType=" + annotationType +
                '}';
    }
}
