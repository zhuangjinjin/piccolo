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
package io.github.ukuz.piccolo.common.properties;

import io.github.ukuz.piccolo.api.config.ConfigurationProperties;
import io.github.ukuz.piccolo.api.config.Properties;
import lombok.Data;

/**
 * @author ukuz90
 */
@ConfigurationProperties(prefix = "piccolo.core")
@Data
public class CoreProperties implements Properties {

    /**
     * netty, nio
     */
    private String epollProvider;
    private Integer maxHeartbeatTime;
    private Integer minHeartbeatTime;
    private Integer maxPacketSize;
    private Integer maxHbTimeoutMs;
    private Integer sessionExpireTime;
    /**
     * nacos,
     */
    private String configCenter;
    /**
     * nacos, zk
     */
    private String srd;

    /**
     * jedis, redisson
     */
    private String cache = "jedis";

    public boolean isUseNettyEpoll() {
        if (!"netty".equals(epollProvider)) {
            return false;
        }
        String osName = System.getProperty("os.name").toLowerCase();
        return osName.startsWith("linux");
    }
}
