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
package io.github.ukuz.piccolo.api.configcenter;

/**
 * @author ukuz90
 */
public interface Configuration {

    default String getString(String key) {
        return getString(key, null);
    }

    default String getString(String key, String defaultValue) {
        String val = getProperty(key, defaultValue);
        return convert(val, String.class);
    }

    default String getProperty(String key, String defaultValue) {
        String returnValue = getProperty(key);
        return returnValue == null ? defaultValue : returnValue;
    }

    String getProperty(String key);

    default <T> T get(String key, Class<T> clazz) {
        String value = getProperty(key);
        return convert(value, clazz);
    }

    default <T> T convert(String obj, Class<T> clazz) {
        if (obj == null) {
            return null;
        }
        if (clazz.isInstance(obj)) {
            return clazz.cast(obj);
        }
        Object value = obj;
        if (Boolean.class.equals(clazz) || Boolean.TYPE.equals(clazz)) {
            value = Boolean.valueOf(obj);
        } else if (Number.class.isAssignableFrom(clazz) || clazz.isPrimitive()) {
            if (Byte.class.equals(clazz) || Byte.TYPE.equals(clazz)) {
                value = Boolean.valueOf(obj);
            } else if (Short.class.equals(clazz) || Short.TYPE.equals(clazz)) {
                value = Short.valueOf(obj);
            } else if (Integer.class.equals(clazz) || Integer.TYPE.equals(clazz)) {
                value = Integer.valueOf(obj);
            } else if (Float.class.equals(clazz) || Float.TYPE.equals(clazz)) {
                value = Float.valueOf(obj);
            } else if (Double.class.equals(clazz) || Double.TYPE.equals(clazz)) {
                value = Double.valueOf(obj);
            } else if (Long.class.equals(clazz) || Long.TYPE.equals(clazz)) {
                value = Long.valueOf(obj);
            }
        } else if (clazz.isEnum()) {
            value = Enum.valueOf(clazz.asSubclass(Enum.class), obj);
        }

        return clazz.cast(value);
    }
}
