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
import io.github.ukuz.piccolo.api.common.utils.StringUtils;
import io.github.ukuz.piccolo.api.external.common.utils.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.Set;

/**
 * @author ukuz90
 */
public class ClassPathScanner {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClassPathScanner.class);
    private static final char PATH_SEPARATOR = '/';
    private static final char PACKAGE_SEPARATOR = '.';
    private static final String CLASS_EXTENSION = ".class";

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

    public Set<Class> scan(String[] scanPackages) throws ClassNotFoundException, IOException {
        Set<Class> result = new HashSet<>();
        String javaPath = System.getProperty("java.class.path");
        String pathSeparator = System.getProperty("path.separator");
        for (String p :javaPath.split(pathSeparator)) {
            int prefixIndex = p.indexOf(":");
            if (prefixIndex == 1) {
                p = StringUtils.uncapitalize(p);
            }
            File baseDir = new File(p);
            Path basePath = baseDir.toPath();
            for (String scanPackage : scanPackages) {
                String scanPath = scanPackage.replace(PACKAGE_SEPARATOR, PATH_SEPARATOR);
                File file = new File(baseDir, scanPath);
                if (file.exists()) {
                    if (LOGGER.isInfoEnabled()) {
                        LOGGER.info("scan path: {}", file.getAbsolutePath());
                    }
                    doCandidate(basePath, file.toPath(), result);
                }
            }
        }

        return result;
    }

    private void doCandidate(Path basePath, Path scanPath, Set<Class> candidatedClassSet) throws IOException, ClassNotFoundException {
        Set<String> classNameSet = new HashSet<>();
        Files.walkFileTree(scanPath, new ClassFileVisitor(basePath, classNameSet));
        for (String className : classNameSet) {
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

    private class ClassFileVisitor extends SimpleFileVisitor<Path> {

        private Set<String> candidateClassName;
        private Path basePath;

        public ClassFileVisitor(Path basePath, Set<String> candidateClassName) {
            this.candidateClassName = candidateClassName;
            this.basePath = basePath;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            FileVisitResult result = super.visitFile(file, attrs);
            if (file.toFile().getName().endsWith(CLASS_EXTENSION)) {
                Path classFilePath = file.subpath(basePath.getNameCount(), file.getNameCount());
                String className = classFilePath.toString().replace(PATH_SEPARATOR, PACKAGE_SEPARATOR);
                className = className.substring(0, className.length() - CLASS_EXTENSION.length());
                candidateClassName.add(className);
            }
            return result;
        }
    }

}
