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
package io.github.ukuz.piccolo.monitor;

import io.github.ukuz.piccolo.api.common.Monitor;
import io.github.ukuz.piccolo.api.service.AbstractService;
import io.github.ukuz.piccolo.api.service.ServiceException;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author ukuz90
 */
public class MonitorService extends AbstractService implements Monitor {

    @Override
    public void init() throws ServiceException {
        Metrics.addRegistry(new PrometheusMeterRegistry(PrometheusConfig.DEFAULT));
    }

    @Override
    public String scrape() {
        List<MeterRegistry> meterRegistries = Metrics.globalRegistry.getRegistries()
                .stream()
                .filter(meterRegistry -> meterRegistry instanceof PrometheusMeterRegistry)
                .collect(Collectors.toList());
        if (meterRegistries.size() < 1) {
            return EMPTY;
        }
        MeterRegistry registry = meterRegistries.get(0);
        if (registry instanceof PrometheusMeterRegistry) {
            return monitor((PrometheusMeterRegistry) registry);
        }

        return EMPTY;
    }

    private String monitor(PrometheusMeterRegistry registry) {
        return registry.scrape();
    }
}
