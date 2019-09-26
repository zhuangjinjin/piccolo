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
package io.github.ukuz.piccolo.registry.nacos;

import com.alibaba.nacos.api.naming.pojo.Instance;
import io.github.ukuz.piccolo.api.PiccoloContext;
import io.github.ukuz.piccolo.api.config.properties.NacosProperties;
import io.github.ukuz.piccolo.api.external.common.Assert;
import io.github.ukuz.piccolo.api.service.AbstractService;
import io.github.ukuz.piccolo.api.service.ServiceException;
import io.github.ukuz.piccolo.api.service.ServiceRegistryAndDiscovery;
import io.github.ukuz.piccolo.api.service.discovery.DefaultServiceInstance;
import io.github.ukuz.piccolo.api.service.discovery.ServiceListener;
import io.github.ukuz.piccolo.registry.nacos.listener.NacosCacheListener;
import io.github.ukuz.piccolo.registry.nacos.manager.NacosManager;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author ukuz90
 */
public class NacosServiceRegistryAndDiscovery extends AbstractService implements ServiceRegistryAndDiscovery<DefaultServiceInstance> {

    private NacosManager nacosManager;

    @Override
    public void init(PiccoloContext context) throws ServiceException {
        NacosProperties prop = context.getProperties(NacosProperties.class);
        nacosManager = new NacosManager(prop, WATCH_PATH);
        nacosManager.init();
    }

    @Override
    public List<DefaultServiceInstance> lookup(String serviceId) {
        List<Instance> instanceList = nacosManager.getDirectory().getAllHealthyInstances(serviceId);
        if (instanceList.isEmpty()) {
            return Collections.emptyList();
        }
        return instanceList.stream()
                .map(NacosInstanceConverter::covert)
                .collect(Collectors.toList());
    }

    @Override
    public void subscribe(String serviceId, ServiceListener<DefaultServiceInstance> listener) {
        NacosCacheListener nacosCacheListener = new NacosCacheListener(listener, WATCH_PATH, serviceId);
        nacosCacheListener.set(lookup(serviceId));
        nacosManager.getDirectory().subscribeListener(serviceId, nacosCacheListener);
    }

    @Override
    public void unsubcribe(String serviceId, ServiceListener<DefaultServiceInstance> listener) {
        NacosCacheListener nacosCacheListener = new NacosCacheListener(listener, WATCH_PATH, serviceId);
        nacosManager.getDirectory().unsubscribeListener(serviceId, nacosCacheListener);
    }

    @Override
    public void registry(DefaultServiceInstance registration) {
        Assert.notNull(registration, "registration must not be null");
        nacosManager.getDirectory().registerInstance(
                registration.getServiceId(),
                registration.getHost(),
                registration.getPort(),
                null);
    }

    @Override
    public void deregistry(DefaultServiceInstance registration) {
        Assert.notNull(registration, "registration must not be null");
        nacosManager.getDirectory().deregisterInstance(
                registration.getServiceId(),
                registration.getHost(),
                registration.getPort(),
                null);
    }
}
