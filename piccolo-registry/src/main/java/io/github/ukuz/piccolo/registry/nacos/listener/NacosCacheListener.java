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
package io.github.ukuz.piccolo.registry.nacos.listener;

import com.alibaba.nacos.api.naming.listener.Event;
import com.alibaba.nacos.api.naming.listener.EventListener;
import com.alibaba.nacos.api.naming.listener.NamingEvent;
import com.alibaba.nacos.api.naming.utils.NamingUtils;
import io.github.ukuz.piccolo.api.service.discovery.DefaultServiceInstance;
import io.github.ukuz.piccolo.api.service.discovery.ServiceListener;
import io.github.ukuz.piccolo.registry.nacos.NacosInstanceConverter;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author ukuz90
 */
public class NacosCacheListener implements EventListener {

    private final ServiceListener listener;
    private final String group;
    private final String serviceName;
    private final Set<DefaultServiceInstance> cache;

    public NacosCacheListener(ServiceListener listener, String group, String serviceName) {
        this.listener = listener;
        this.group = group;
        this.serviceName = serviceName;
        cache = new HashSet<>();
    }

    public void set(List<DefaultServiceInstance> instances) {
        cache.clear();
        cache.addAll(instances);
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof NamingEvent) {
            NamingEvent evt = (NamingEvent) event;
            if (NacosInstanceConverter.toNacosString(serviceName).equals(NamingUtils.getServiceName(evt.getServiceName()))) {

                final List<DefaultServiceInstance> newInstances = evt.getInstances().stream()
                        .map(NacosInstanceConverter::covert)
                        .collect(Collectors.toList());

                List<DefaultServiceInstance> updated = newInstances.stream()
                        .filter(cache::contains)
                        .collect(Collectors.toList());

                List<DefaultServiceInstance> added = newInstances.stream()
                        .filter(instance -> !cache.contains(instance))
                        .collect(Collectors.toList());

                List<DefaultServiceInstance> removed = cache.stream()
                        .filter(instance -> !newInstances.contains(instance))
                        .collect(Collectors.toList());

                updated.forEach(listener::onServiceUpdated);
                added.forEach(listener::onServiceAdded);
                removed.forEach(listener::onServiceDeleted);

                cache.clear();
                cache.addAll(newInstances);

            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        NacosCacheListener that = (NacosCacheListener) o;
        return Objects.equals(group, that.group) &&
                Objects.equals(serviceName, that.serviceName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(group, serviceName);
    }
}
