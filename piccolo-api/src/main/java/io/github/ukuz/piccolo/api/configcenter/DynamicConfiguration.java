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

import io.github.ukuz.piccolo.api.service.Service;
import io.github.ukuz.piccolo.api.spi.Spi;

/**
 * @author ukuz90
 */
@Spi
public interface DynamicConfiguration extends Configuration, Service {

    String DEFAULT = "nacos";
    String DEFAULT_GROUP = "PICCOLO";

    default void setProperty(String key, String val) {
        setProperty(key, DEFAULT_GROUP, val);
    }

    void setProperty(String key, String group, String val);

    default void addListener(String key, ConfigurationListener listener) {
        addListener(key, DEFAULT_GROUP, listener);
    }

    void addListener(String key, String group, ConfigurationListener listener);

}
