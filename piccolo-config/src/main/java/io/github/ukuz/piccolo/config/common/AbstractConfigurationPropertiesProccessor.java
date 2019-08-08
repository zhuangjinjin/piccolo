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
package io.github.ukuz.piccolo.config.common;

import io.github.ukuz.piccolo.api.config.ConfigurationProperties;
import io.github.ukuz.piccolo.api.config.Properties;
import io.github.ukuz.piccolo.api.external.common.utils.ClassUtils;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * @author ukuz90
 */
public abstract class AbstractConfigurationPropertiesProccessor<T extends Configuration> implements ConfigurationPropertiesProcessor<T> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Map<Class, BiFunction<String, T, Object>> wireTypeFunction = new HashMap<>();

    @Override
    public void init() {
        wireTypeFunction.putIfAbsent(int.class, (key, configuration) -> configuration.getInt(key, 0));
        wireTypeFunction.putIfAbsent(Integer.class, (key, configuration) -> configuration.getInteger(key, Integer.valueOf(0)));
        wireTypeFunction.putIfAbsent(byte.class, (key, configuration) -> configuration.getByte(key, (byte) 0));
        wireTypeFunction.putIfAbsent(Byte.class, (key, configuration) -> configuration.getByte(key, (byte) 0));
        wireTypeFunction.putIfAbsent(boolean.class, (key, configuration) -> configuration.getBoolean(key, false));
        wireTypeFunction.putIfAbsent(Boolean.class, (key, configuration) -> configuration.getBoolean(key, Boolean.FALSE));
        wireTypeFunction.putIfAbsent(short.class, (key, configuration) -> configuration.getShort(key, (short) 0));
        wireTypeFunction.putIfAbsent(Short.class, (key, configuration) -> configuration.getShort(key, (short) 0));
        wireTypeFunction.putIfAbsent(long.class, (key, configuration) -> configuration.getLong(key, 0));
        wireTypeFunction.putIfAbsent(Long.class, (key, configuration) -> configuration.getLong(key, Long.valueOf(0)));
        wireTypeFunction.putIfAbsent(float.class, (key, configuration) -> configuration.getFloat(key, 0));
        wireTypeFunction.putIfAbsent(Float.class, (key, configuration) -> configuration.getFloat(key, Float.valueOf(0)));
        wireTypeFunction.putIfAbsent(double.class, (key, configuration) -> configuration.getDouble(key, 0));
        wireTypeFunction.putIfAbsent(Double.class, (key, configuration) -> configuration.getDouble(key, Double.valueOf(0)));
        wireTypeFunction.putIfAbsent(String.class, (key, configuration) -> configuration.getString(key, ""));

    }

    @Override
    public void process(T configuration, Properties properties) {
        Field[] fields = properties.getClass().getDeclaredFields();
        ConfigurationProperties annotation = properties.getClass().getAnnotation(ConfigurationProperties.class);
        for (Field field : fields) {
            Object value = fetchProperty(annotation.prefix(), field, configuration);
            field.setAccessible(true);
            try {
                field.set(properties, value);
            } catch (IllegalAccessException e) {
                logger.error("process failure, class: {}, field: {}, cause: {}", properties.getClass().getName(), field.getName(), e.getMessage());
                e.printStackTrace();
            }
            field.setAccessible(false);
        }
    }

    protected Object fetchProperty(String prefix, Field field, T configuration) throws WireTypeNotSupportException {
        if (wireTypeFunction.containsKey(field.getGenericType().getClass())) {
            throw new WireTypeNotSupportException("field wire type not support,"
                    + " name: " + ClassUtils.simpleClassName(field.getDeclaringClass()) +"."+ field.getName()
                    + " type: " + field.getGenericType());
        }
        String key = getPropertyKey(prefix, field.getName());
        return wireTypeFunction.get(field.getType()).apply(key, configuration);
    }

    protected String getPropertyKey(String prefix, String fieldName) {
        String[] names = StringUtils.splitByCharacterTypeCamelCase(fieldName);
        return prefix + "." + StringUtils.join(names, "-").toLowerCase();
    }

}
