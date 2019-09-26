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
package io.github.ukuz.piccolo.api.service.discovery;

import io.github.ukuz.piccolo.api.service.registry.Registration;
import lombok.Setter;

import java.util.Map;
import java.util.Objects;

/**
 * @author ukuz90
 */
@Setter
public class DefaultServiceInstance implements ServiceInstance, Registration {

    private String serviceId;
    private String instanceId;
    private String host;
    private int port;
    private boolean isPersistent;
    private Map<String, String> metaData;

    public DefaultServiceInstance serviceId(String serviceId) {
        this.serviceId = serviceId;
        return this;
    }

    public DefaultServiceInstance instanceId(String instanceId) {
        this.instanceId = instanceId;
        return this;
    }

    public DefaultServiceInstance host(String host) {
        this.host = host;
        return this;
    }

    public DefaultServiceInstance port(int port) {
        this.port = port;
        return this;
    }

    public DefaultServiceInstance isPersistent(boolean persistent) {
        isPersistent = persistent;
        return this;
    }

    public DefaultServiceInstance metaData(Map<String, String> metaData) {
        this.metaData = metaData;
        return this;
    }

    public static DefaultServiceInstance build() {
        return new DefaultServiceInstance();
    }

    @Override
    public String getServiceId() {
        return serviceId;
    }

    @Override
    public String getInstanceId() {
        return instanceId;
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public Map<String, String> getMetaData(String key) {
        return metaData;
    }

    @Override
    public boolean isPersistent() {
        return isPersistent;
    }

    @Override
    public String getServicePath() {
        return serviceId + "/" + getHostAndPort();
    }

    @Override
    public String toString() {
        return "DefaultServiceInstance{" +
                "serviceId='" + serviceId + '\'' +
                ", instanceId='" + instanceId + '\'' +
                ", host='" + host + '\'' +
                ", port=" + port +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DefaultServiceInstance that = (DefaultServiceInstance) o;
        return port == that.port &&
                Objects.equals(serviceId, that.serviceId) &&
                Objects.equals(host, that.host);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(serviceId);
        result = result * 31 + Objects.hashCode(host);
        return result * 31 + port;
    }

}
