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
import io.github.ukuz.piccolo.api.external.common.utils.ClassUtils;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

/**
 * @author ukuz90
 */
public class ClassPathScanner {

    private static final char CLASSPATH_RESOURCE_PATH_SEPARATOR = '/';
    private static final char PACKAGE_SEPARATOR_CHAR = '.';
    private static final String CLASS_NAME_SUFFIX = ".class";
    private static final String PACKAGE_SEPARATOR_REGEX = "\\.";

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
            int baseNameCount = scanPackage.split(PACKAGE_SEPARATOR_REGEX).length;
            String scanPath = scanPackage.replace(PACKAGE_SEPARATOR_CHAR, CLASSPATH_RESOURCE_PATH_SEPARATOR);
            Path baseDir = Paths.get(findClassLoader().getResource(scanPath).toURI());
            doCandidate(baseDir, baseNameCount, baseDir.toFile(), result);
        }

        return result;
    }

    private void doCandidate(Path baseDir, int baseNameCount, File file, Set<Class> candidatedClassSet) throws ClassNotFoundException {
        if (file.isDirectory()) {
            File[] childrenFiles = file.listFiles();
            for (File childrenFile : childrenFiles) {
                doCandidate(baseDir, baseNameCount, childrenFile, candidatedClassSet);
            }
        } else {
            Path path = Paths.get(file.toURI());
            String className = null;
            if (!path.startsWith(baseDir)) {
               return;
            }
            Path newPath = path.subpath(baseDir.getNameCount() - baseNameCount, path.getNameCount());
            StringBuilder sb = new StringBuilder();
            newPath.forEach(p -> sb.append(p.toString()).append(PACKAGE_SEPARATOR_CHAR));
            className = sb.substring(0, sb.lastIndexOf(CLASS_NAME_SUFFIX));
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
