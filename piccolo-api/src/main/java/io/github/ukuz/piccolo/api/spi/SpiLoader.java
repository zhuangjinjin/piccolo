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

import io.github.ukuz.piccolo.api.annotation.AnnotationTypeFilter;
import io.github.ukuz.piccolo.api.annotation.Order;
import io.github.ukuz.piccolo.api.common.Holder;
import io.github.ukuz.piccolo.api.common.OrderComparator;
import io.netty.util.internal.StringUtil;

import java.util.List;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Extension Mechanism (Thread safe)
 *
 * @author ukuz90
 */
public class SpiLoader<T> {

    private static final ConcurrentHashMap<Class<?>, SpiLoader> LOADERS = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Class<?>, Holder<Object>> INSTANCES = new ConcurrentHashMap<>();
    private static final String PLUGIN_SERVICES = "META-INF" + File.separator + "services";
    private static final String PLUGIN_PICCOLO = "META-INF" + File.separator + "piccolo";


    private final Holder<Map<String, Class<T>>> cachedClassHolder = new Holder<>();
    private final Holder<AnnotationTypeFilter> annotationTypeFilterHolder = new Holder<>();
    private final Class<T> type;
    private final String primaryExtensionKey;

    private SpiLoader(Class<T> type) {
        this.type = type;
        Spi spi = type.getAnnotation(Spi.class);
        primaryExtensionKey = spi.primary();
    }

    public static SpiLoader getLoader(Class<?> type) {
        if (type == null) {
            throw new IllegalArgumentException("Spi type must not be null");
        }
        if (!type.isInterface()) {
            throw new IllegalArgumentException("Spi type " + type.getName() + " must an interface");
        }
        if (!type.isAnnotationPresent(Spi.class)) {
            throw new IllegalArgumentException("Spi type " + type.getName() + " must annotated with @Spi");
        }
        SpiLoader loader = LOADERS.get(type);
        if (loader == null) {
            LOADERS.putIfAbsent(type, new SpiLoader(type));
            loader = LOADERS.get(type);
        }
        return loader;
    }

    public T getExtension() {
        Map<String, Class<T>> extensionClassMap = getExtensionClass();
        Class<T> primaryClass = null;
        if (StringUtil.isNullOrEmpty(primaryExtensionKey)) {
            //order
            primaryClass = choosePrimaryClass(extensionClassMap);

        } else {
            primaryClass = extensionClassMap.get(primaryExtensionKey);
            if (primaryClass == null) {
                throw new IllegalArgumentException("Can not found " + primaryExtensionKey + " mapping class");
            }
        }

        if (primaryClass == null) {
            throw new IllegalArgumentException("Can not found default extension");
        }

        return getInstance(primaryClass);
    }

    @SuppressWarnings("unchecked")
    private Class<T> choosePrimaryClass(Map<String, Class<T>> extensionClassMap) {
        List<Class<T>> extensionClassList = new ArrayList<>(extensionClassMap.values());

        AnnotationTypeFilter filter = annotationTypeFilterHolder.getValue();
        if (filter == null) {
            synchronized (annotationTypeFilterHolder) {
                filter = annotationTypeFilterHolder.getValue();
                if (filter == null) {
                    filter = new AnnotationTypeFilter(Order.class);
                    annotationTypeFilterHolder.setValue(filter);
                }
            }
        }

        //filter which annotated with @Order
        List<Class<T>> orderExtensionClassList = extensionClassList.stream()
                .filter(filter::match)
                .collect(Collectors.toList());

        if (orderExtensionClassList.isEmpty()) {
            if (extensionClassList.size() == 1) {
                return extensionClassList.get(0);
            }
            //Could not determine primary class
            return null;
        } else if (orderExtensionClassList.size() == 1) {
            return orderExtensionClassList.get(0);
        } else {
            OrderComparator orderComparator = (OrderComparator) SpiLoader.getLoader(OrderComparator.class).getExtension();
            orderExtensionClassList = orderComparator.compare(orderExtensionClassList);
            return orderExtensionClassList.get(0);
        }
    }

    public T getExtension(String key) {
        Map<String, Class<T>> extensionClassMap = getExtensionClass();
        if (StringUtil.isNullOrEmpty(key)) {
            throw new IllegalArgumentException("Key must not be null");
        }
        Class<T> clazz = extensionClassMap.get(key);
        if (clazz == null) {
            throw new IllegalArgumentException("Can not found " + key + " mapping class");
        }
        return getInstance(clazz);
    }

    private T getInstance(Class<T> clazz) {
        Holder holder = INSTANCES.get(clazz);
        if (holder == null) {
            INSTANCES.putIfAbsent(clazz, new Holder<>());
            holder = INSTANCES.get(clazz);
        }
        return getInstance(holder, clazz);
    }

    private T getInstance(Holder holder, Class<T> clazz) {
        Object obj = holder.getValue();
        if (obj == null) {
            synchronized (holder) {
                if (obj == null) {
                    try {
                        obj = clazz.newInstance();
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    holder.setValue(obj);
                }
            }
        }
        return (T) obj;
    }

    /**
     * lazy load(double check)
     * @return
     */
    private Map<String, Class<T>> getExtensionClass() {
        Map<String, Class<T>> extensionClassMap = cachedClassHolder.getValue();
        if (extensionClassMap == null) {
            synchronized (cachedClassHolder) {
                extensionClassMap = cachedClassHolder.getValue();
                if (extensionClassMap == null) {
                    extensionClassMap = loadExtensionClass();
                    cachedClassHolder.setValue(extensionClassMap);
                }
            }
        }
        return extensionClassMap;
    }

    private Map<String, Class<T>> loadExtensionClass() {
        Map<String, Class<T>> extensionClassMap = new HashMap<>(16);
        loadDirectory(extensionClassMap, PLUGIN_SERVICES);
        loadDirectory(extensionClassMap, PLUGIN_PICCOLO);
        return extensionClassMap;
    }

    private void loadDirectory(Map<String, Class<T>> extensionClassMap, String dir) {
        String fileName = dir + File.separator + type.getName();
        Enumeration<URL> urls = null;

        try {
            ClassLoader classLoader = findClassLoader();
            if (classLoader != null) {
                urls = classLoader.getResources(fileName);
            } else {
                urls = ClassLoader.getSystemResources(fileName);
            }
            if (urls == null) {
                return;
            }
            Collections.list(urls)
                    .stream()
                    .filter(url -> url != null)
                    .forEach(url -> loadResource(extensionClassMap, url));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void loadResource(Map<String, Class<T>> extensionClassMap, URL url) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()))) {
            String line;
            while ((line = br.readLine()) != null) {
                int index = line.indexOf("#");
                line = index == -1 ? line.trim() : line.substring(0, index).trim();
                String key = null;
                index = line.indexOf("=");
                if (index != -1) {
                    key = line.substring(0, index);
                    line = line.substring(index+1);
                }
                if (line.length() > 0) {
                    loadClass(extensionClassMap, key, line);
                }
            }

        } catch (IOException e) {

        }
    }

    private void loadClass(Map<String, Class<T>> extensionClassMap, String key, String className) {
        try {
            Class<T> clazz = (Class<T>) findClassLoader().loadClass(className);
            if (StringUtil.isNullOrEmpty(key)) {
                key = getPrefixClassName(clazz);
            }
            extensionClassMap.putIfAbsent(key, clazz);

        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Can not found class " + className);
        }

    }

    private String getPrefixClassName(Class clazz) {
        String prefix;
        int index = clazz.getSimpleName().indexOf(type.getSimpleName());
        if (index != -1) {
            prefix = clazz.getSimpleName().substring(0, index).toLowerCase();
        } else {
            prefix = clazz.getSimpleName().toLowerCase();
        }
        return prefix;
    }

    private ClassLoader findClassLoader() {
        ClassLoader classLoader = null;
        try {
            classLoader = Thread.currentThread().getContextClassLoader();
        } catch (Throwable e) {

        }
        if (classLoader == null) {
            classLoader = this.getClass().getClassLoader();
        }
        if (classLoader == null) {
            classLoader = ClassLoader.getSystemClassLoader();
        }
        return classLoader;
    }

}
