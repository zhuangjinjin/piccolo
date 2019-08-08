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
package io.github.ukuz.piccolo.config.properties;


import io.github.ukuz.piccolo.api.annotation.AnnotationTypeFilter;
import io.github.ukuz.piccolo.api.common.ClassPathScanner;
import io.github.ukuz.piccolo.api.config.ConfigurationProperties;
import io.github.ukuz.piccolo.api.config.Environment;
import io.github.ukuz.piccolo.api.config.EnvironmentException;
import io.github.ukuz.piccolo.api.config.Properties;
import io.github.ukuz.piccolo.api.external.common.utils.ClassUtils;
import io.github.ukuz.piccolo.config.common.ConfigurationPropertiesProcessor;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author ukuz90
 */
public class PropertiesEnvironment implements Environment {

    private final Configurations configs;
    private final ConfigurationPropertiesProcessor processor;

    private final Logger logger = LoggerFactory.getLogger(PropertiesEnvironment.class);

    private final ConcurrentMap<String, Properties> configMap = new ConcurrentHashMap<>();
    private final AtomicBoolean isScan = new AtomicBoolean();


    private static final String CONF_FILE_NAME = "conf" + File.separator + "piccolo.properties";
    private static final Properties EMPTY = new EmptyProperties();

    public PropertiesEnvironment() {
        this.configs = new Configurations();
        this.processor = new PropertiesConfigurationPropertiesProcessor();

        this.processor.init();
    }

    @Override
    public void scanAllProperties() {
        if (!isScan.compareAndSet(false, true)) {
            throw new EnvironmentException("repeat scan properties");
        }

        // 只扫描@ConfigurationProperties的类
        ClassPathScanner scanner = new ClassPathScanner();
        AnnotationTypeFilter filter = new AnnotationTypeFilter(ConfigurationProperties.class);
        scanner.addIncludeFilter(filter);

        try {
            Set<Class> classes = scanner.scan(new String[]{"io.github.ukuz.piccolo"});
            classes.parallelStream()
                    .filter(Properties.class::isAssignableFrom)
                    .forEach(this::newInstance);
        } catch (Exception e) {
            throw new EnvironmentException(e);
        }

    }

    private void newInstance(Class<?> clazz) {
        try {
            Properties properties = (Properties) clazz.newInstance();
            configMap.putIfAbsent(clazz.getName(), properties);
        } catch (InstantiationException e) {
            logger.error("new instance failure, cause: {}", e.getMessage());
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            logger.error("new instance failure, cause: {}", e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void load() throws EnvironmentException {
        URL url = findClassLoader().getResource(CONF_FILE_NAME);
        try {
            PropertiesConfiguration config = configs.properties(url);
            synchronized (this) {
                configMap.values()
                        .forEach(properties -> processor.process(config, properties));
            }
        } catch (ConfigurationException e) {
            throw new EnvironmentException(e);
        }
    }

    @Override
    public <T extends Properties> T getProperties(Class<T> clazz) {
        return (T) configMap.getOrDefault(clazz.getName(), EMPTY);
    }

    private ClassLoader findClassLoader() {
        return ClassUtils.getClassLoader(PropertiesEnvironment.class);
    }

    public static class EmptyProperties implements Properties {}
}
