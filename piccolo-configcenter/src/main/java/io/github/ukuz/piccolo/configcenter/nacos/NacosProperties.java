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
package io.github.ukuz.piccolo.configcenter.nacos;

import com.alibaba.nacos.api.PropertyKeyConst;
import io.github.ukuz.piccolo.api.config.ConfigurationProperties;
import io.github.ukuz.piccolo.api.config.Properties;
import lombok.Data;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * @author ukuz90
 */
@ConfigurationProperties(prefix = "piccolo.nacos")
@Data
public class NacosProperties implements Properties {

    private String serverAddress;
    private String accessKey;
    private String secretKey;
    private String namespace;

    public java.util.Properties build() {
        PropertiesHolder propsHolder = new PropertiesHolder(new java.util.Properties());
        Optional.ofNullable(serverAddress).ifPresent(propsHolder.fill(PropertyKeyConst.SERVER_ADDR));
        Optional.ofNullable(accessKey).ifPresent(propsHolder.fill(PropertyKeyConst.ACCESS_KEY));
        Optional.ofNullable(secretKey).ifPresent(propsHolder.fill(PropertyKeyConst.SECRET_KEY));
        Optional.ofNullable(namespace).ifPresent(propsHolder.fill(PropertyKeyConst.NAMESPACE));

        return propsHolder.getProps();
    }

    private class PropertiesHolder {

        final java.util.Properties props;

        public PropertiesHolder(java.util.Properties props) {
            this.props = props;
        }

        public Consumer<String> fill(String key) {
            return (val) -> props.setProperty(key, val);
        }

        public java.util.Properties getProps() {
            return props;
        }
    }
}
