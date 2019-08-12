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
package io.github.ukuz.piccolo.registry.zookeeper.properties;

import io.github.ukuz.piccolo.api.config.ConfigurationProperties;
import io.github.ukuz.piccolo.api.config.Properties;
import lombok.Data;

/**
 * @author ukuz90
 */
@ConfigurationProperties(prefix = "piccolo.zookeeper")
@Data
public class ZooKeeperProperties implements Properties {

    /**
     * host (eg: ip1:port1,ip2:port2,...)
     */
    private String host;
    private String ns;
    private String digest;
    private int sessionTimeoutMs;
    private int connectionTimeoutMs;
    private int maxRetries;
    private int baseSleepTimeMs;
    private int maxSleepMs;


}
