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

import io.github.ukuz.piccolo.api.common.utils.NetworkUtils;
import io.github.ukuz.piccolo.api.common.utils.StringUtils;
import io.github.ukuz.piccolo.api.config.ConfigurationProperties;
import io.github.ukuz.piccolo.api.config.Properties;
import lombok.Data;

/**
 * @author ukuz90
 */
@ConfigurationProperties(prefix = "piccolo.net")
@Data
public class NetProperties implements Properties {

    private String localIp;
    private String publicIp;
    private String wsPath;
    private boolean userOfflineOnConnectionClose;

    private SslNestedProperties wsSsl;

    private ServerNestedProperties connectServer;
    private ServerNestedProperties gatewayServer;
    private ServerNestedProperties wsServer;

    private TrafficNestedProperties connectServerTraffic;
    private TrafficNestedProperties gatewayServerTraffic;

    @Data
    public class SslNestedProperties implements Properties {
        private boolean enable;
        private String crtFilename;
        private String keyFilename;
    }

    @Data
    public class ServerNestedProperties implements Properties {
        private int bindPort;
        private String bindIp;
        private String registerIp;
        private int sndBuf;
        private int rcvBuf;
        private int writeWaterMarkLow;
        private int writeWaterMarkHigh;
    }

    @Data
    public class TrafficNestedProperties implements Properties {
        private boolean enabled;
        private long checkIntervalMs;
        private int writeGlobalLimit;
        private int readGlobalLimit;
        private int writeChannelLimit;
        private int readChannelLimit;
    }

    public String getLocalIp() {
        if (StringUtils.hasText(localIp)) {
            return localIp;
        }
        return NetworkUtils.getLocalAddress();
    }

    public String getPublicIp() {
        if (StringUtils.hasText(publicIp)) {
            return publicIp;
        }
        String localAddress = getLocalIp();
        String publicAddress = NetworkUtils.getExtranetAddress();
        return StringUtils.hasText(publicAddress) ? publicAddress : localAddress;
    }

}
