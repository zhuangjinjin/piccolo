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
package io.github.ukuz.piccolo.api.connection;

import io.github.ukuz.piccolo.api.router.ClientClassifier;
import io.github.ukuz.piccolo.api.spi.SpiLoader;

/**
 * @author ukuz90
 */
public class SessionContext {

    private String osName;
    private String osVersion;
    private String clientVersion;
    private String userId;
    private String deviceId;
    private String tags;
    private int heartbeat = 10000;
    private byte clientType;
    private Cipher cipher;

    public void changeCipher(Cipher cipher) {
        this.cipher = cipher;
    }

    public String getOsName() {
        return osName;
    }

    public void setOsName(String osName) {
        this.osName = osName;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }

    public String getClientVersion() {
        return clientVersion;
    }

    public void setClientVersion(String clientVersion) {
        this.clientVersion = clientVersion;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public int getHeartbeat() {
        return heartbeat;
    }

    public void setHeartbeat(int heartbeat) {
        this.heartbeat = heartbeat;
    }

    public byte getClientType() {
        if (clientType == 0) {
            clientType = SpiLoader.getLoader(ClientClassifier.class).getExtension().getClientType(osName);
        }
        return clientType;
    }

    public boolean isSecurity() {
        return cipher != null;
    }

    public Cipher getCipher() {
        return cipher;
    }

    public void setClientType(byte clientType) {
        this.clientType = clientType;
    }
}
