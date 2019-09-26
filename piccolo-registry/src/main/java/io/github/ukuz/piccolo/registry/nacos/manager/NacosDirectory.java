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
package io.github.ukuz.piccolo.registry.nacos.manager;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.listener.EventListener;
import com.alibaba.nacos.api.naming.pojo.Instance;
import io.github.ukuz.piccolo.api.external.common.Assert;
import static io.github.ukuz.piccolo.registry.nacos.NacosInstanceConverter.toNacosString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

/**
 * @author ukuz90
 */
public class NacosDirectory {

    private static final Logger LOGGER = LoggerFactory.getLogger(NacosDirectory.class);
    private final NamingService namingService;
    private final String group;

    public NacosDirectory(NamingService namingService, String group) {
        this.namingService = namingService;
        this.group = toNacosString(group);
    }

    public List<Instance> getAllInstances(String serviceId) {
        Assert.notEmptyString(serviceId, "serviceId must not be empty");
        serviceId = toNacosString(serviceId);
        try {
            return namingService.getAllInstances(serviceId, group);
        } catch (NacosException e) {
            LOGGER.warn("getAllInstances failure, err: {}", e.getCause());
            return Collections.emptyList();
        }
    }

    public List<Instance> getAllHealthyInstances(String serviceId) {
        Assert.notEmptyString(serviceId, "serviceId must not be empty");
        serviceId = toNacosString(serviceId);
        try {
            return namingService.selectInstances(serviceId, group, true );
        } catch (NacosException e) {
            LOGGER.warn("getAllHealthyInstances failure, err: {}", e.getCause());
            return Collections.emptyList();
        }
    }

    public void registerInstance(String serviceName, String ip, int port, String clusterName) {
        Assert.notEmptyString(serviceName, "serviceName must not be empty");
        Assert.notEmptyString(ip, "ip must not be empty");
        Assert.isTrue(port > 0, "port must great than 0");
        serviceName = toNacosString(serviceName);
        ip = toNacosString(ip);
        clusterName = toNacosString(clusterName);
        try {
            namingService.registerInstance(serviceName, group, ip, port, clusterName);
        } catch (NacosException e) {
            LOGGER.warn("registerInstance failure, err: {}", e.getCause());
        }
    }

    public void deregisterInstance(String serviceName, String ip, int port, String clusterName) {
        Assert.notEmptyString(serviceName, "serviceName must not be empty");
        Assert.notEmptyString(ip, "ip must not be empty");
        Assert.isTrue(port > 0, "port must great than 0");
        serviceName = toNacosString(serviceName);
        ip = toNacosString(ip);
        clusterName = toNacosString(clusterName);
        try {
            namingService.deregisterInstance(serviceName, group, ip, port, clusterName);
        } catch (NacosException e) {
            LOGGER.warn("deregisterInstance failure, err: {}", e.getCause());
        }
    }

    public void subscribeListener(String serviceName, EventListener eventListener) {
        Assert.notEmptyString(serviceName, "serviceName must not be empty");
        Assert.notNull(eventListener, "eventListener must not be null");
        serviceName = toNacosString(serviceName);
        try {
            namingService.subscribe(serviceName, group, eventListener);
        } catch (NacosException e) {
            LOGGER.warn("subscribeListener failure, err: {}", e.getCause());
        }
    }

    public void unsubscribeListener(String serviceName, EventListener eventListener) {
        Assert.notEmptyString(serviceName, "serviceName must not be empty");
        Assert.notNull(eventListener, "eventListener must not be null");
        serviceName = toNacosString(serviceName);
        try {
            namingService.unsubscribe(serviceName, group, eventListener);
        } catch (NacosException e) {
            LOGGER.warn("subscribeListener failure, err: {}", e.getCause());
        }
    }

}
