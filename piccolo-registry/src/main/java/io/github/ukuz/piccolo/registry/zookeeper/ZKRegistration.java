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
package io.github.ukuz.piccolo.registry.zookeeper;

import io.github.ukuz.piccolo.api.service.discovery.ServiceInstance;
import io.github.ukuz.piccolo.api.service.registry.Registration;
import org.apache.curator.utils.ZKPaths;

import java.util.Map;

/**
 * @author ukuz90
 */
public class ZKRegistration implements Registration, ServiceInstance {

    private ServiceInstance instance;

    public ZKRegistration(ServiceInstance instance) {
        this.instance = instance;
    }

    @Override
    public String getServiceId() {
        return instance.getServiceId();
    }

    @Override
    public String getInstanceId() {
        return instance.getInstanceId();
    }

    @Override
    public String getHost() {
        return instance.getHost();
    }

    @Override
    public int getPort() {
        return instance.getPort();
    }

    @Override
    public Map<String, String> getMetaData(String key) {
        return instance.getMetaData(key);
    }

    @Override
    public boolean isPersistent() {
        return instance.isPersistent();
    }

    @Override
    public String getServicePath() {
        return getServiceId() + ZKPaths.PATH_SEPARATOR + getHostAndPort();
    }

    public static ZKRegistration build(ServiceInstance instance) {
        return new ZKRegistration(instance);
    }
}
