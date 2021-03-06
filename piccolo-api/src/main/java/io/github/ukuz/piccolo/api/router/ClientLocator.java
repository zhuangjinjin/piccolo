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
package io.github.ukuz.piccolo.api.router;

import io.github.ukuz.piccolo.api.connection.Connection;
import io.github.ukuz.piccolo.api.connection.SessionContext;
import io.github.ukuz.piccolo.api.service.Client;
import io.github.ukuz.piccolo.api.spi.SpiLoader;
import lombok.Getter;
import lombok.Setter;

/**
 * @author ukuz90
 */
public final class ClientLocator {

    private String host;
    private int port;
    private String osName;
    private String osVersion;
    private String clientVersion;
    private String deviceId;
    private String connId;
    private transient byte clientType;

    public byte getClientType() {
        if (clientType == 0) {
            clientType = SpiLoader.getLoader(ClientClassifier.class).getExtension().getClientType(osName);
        }
        return clientType;
    }

    public boolean isOnline() {
        return connId != null;
    }

    public boolean isOffline() {
        return connId == null;
    }

    public ClientLocator offline() {
        this.connId = null;
        return this;
    }

    public boolean isSameMachine(String host, int port) {
        return this.port == port && this.host.equals(host);
    }

    public String getHostAndPort() {
        return host + ":" + port;
    }

    public ClientLocator setHost(String host) {
        this.host = host;
        return this;
    }

    public ClientLocator setPort(int port) {
        this.port = port;
        return this;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
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

    public ClientLocator setOsVersion(String osVersion) {
        this.osVersion = osVersion;
        return this;
    }

    public String getClientVersion() {
        return clientVersion;
    }

    public ClientLocator setClientVersion(String clientVersion) {
        this.clientVersion = clientVersion;
        return this;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public ClientLocator setDeviceId(String deviceId) {
        this.deviceId = deviceId;
        return this;
    }

    public String getConnId() {
        return connId;
    }

    public ClientLocator setConnId(String connId) {
        this.connId = connId;
        return this;
    }

    public ClientLocator setClientType(byte clientType) {
        this.clientType = clientType;
        return this;
    }

    public static ClientLocator from(Connection connection) {
        SessionContext context = connection.getSessionContext();
        ClientLocator clientLocator = new ClientLocator();
        clientLocator.clientVersion = context.getClientVersion();
        clientLocator.osName = context.getOsName();
        clientLocator.osVersion = context.getOsVersion();
        clientLocator.deviceId = context.getDeviceId();
        clientLocator.connId = connection.getId();
        return clientLocator;
    }

    public String toJson() {
        return "{"
                + "\"port\":" + port
                + (host == null ? "" : ",\"host\":\"" + host + "\"")
                + (deviceId == null ? "" : ",\"deviceId\":\"" + deviceId + "\"")
                + (osName == null ? "" : ",\"osName\":\"" + osName + "\"")
                + (osVersion == null ? "" : ",\"osVersion\":\"" + osVersion + "\"")
                + (clientVersion == null ? "" : ",\"clientVersion\":\"" + clientVersion + "\"")
                + (connId == null ? "" : ",\"connId\":\"" + connId + "\"")
                + "}";

    }

    @Override
    public String toString() {
        return "ClientLocator{" +
                "host='" + host + '\'' +
                ", port=" + port +
                ", osName='" + osName + '\'' +
                ", osVersion='" + osVersion + '\'' +
                ", clientVersion='" + clientVersion + '\'' +
                ", deviceId='" + deviceId + '\'' +
                ", connId='" + connId + '\'' +
                '}';
    }
}