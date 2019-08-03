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
import io.github.ukuz.piccolo.api.common.utils.ClassUtils;

import java.io.File;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

/**
 * @author ukuz90
 */
public class ClassPathScanner {

    private Set<AnnotationTypeFilter> includes = new HashSet<>();
    private Set<AnnotationTypeFilter> excludes = new HashSet<>();

    public void addIncludeFilter(AnnotationTypeFilter filter) {
        if (excludes.contains(filter)) {
            throw new IllegalArgumentException(filter + " was already in exclude filter set");
        }
        includes.add(filter);
    }

    public void addExcludeFilter(AnnotationTypeFilter filter) {
        if (includes.contains(filter)) {
            throw new IllegalArgumentException(filter + " was already in include filter set");
        }
        excludes.add(filter);
    }

    public Set<Class> scan(String[] scanPackages) throws ClassNotFoundException, URISyntaxException {
        Set<Class> result = new HashSet<>();
        for (String scanPackage : scanPackages) {
            String scanPath = scanPackage.replaceAll("\\.", File.separator);
            File file = new File(findClassLoader().getResource(scanPath).toURI());
            doCandidate(scanPath, file, result);
        }

        return result;
    }

    private void doCandidate(String scanPath, File file, Set<Class> candidatedClassSet) throws ClassNotFoundException {
        if (file.isDirectory()) {
            File[] childrenFiles = file.listFiles();
            for (File childrenFile : childrenFiles) {
                doCandidate(scanPath, childrenFile, candidatedClassSet);
            }
        } else {
            int start = file.getPath().indexOf(scanPath);
            int end = file.getPath().lastIndexOf(".class");
            String className = file.getPath().substring(start, end).replaceAll(File.separator, ".");
            Class clazz = findClassLoader().loadClass(className);
            if (candidate(clazz)) {
                candidatedClassSet.add(clazz);
            }
        }
    }

    private boolean candidate(Class clazz) {
        for (AnnotationTypeFilter filter : excludes) {
            if (filter.match(clazz)) {
                return false;
            }
        }
        for (AnnotationTypeFilter filter : includes) {
            if (filter.match(clazz)) {
                return true;
            }
        }
        return includes.isEmpty();
    }

    private ClassLoader findClassLoader() {
        return ClassUtils.getClassLoader(this.getClass());
    }

}
