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

import io.github.ukuz.piccolo.api.PiccoloContext;
import io.github.ukuz.piccolo.api.common.Monitor;
import io.github.ukuz.piccolo.api.common.threadpool.ExecutorFactory;
import io.github.ukuz.piccolo.api.service.AbstractService;
import io.github.ukuz.piccolo.api.service.ServiceException;
import io.github.ukuz.piccolo.monitor.data.MetricsCollector;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author ukuz90
 */
public class MonitorService extends AbstractService implements Monitor {

    private MetricsCollector metricsCollector;
    private static final Logger LOGGER = LoggerFactory.getLogger(MonitorService.class);

    @Override
    public void init(PiccoloContext context) throws ServiceException {
        Metrics.addRegistry(new PrometheusMeterRegistry(PrometheusConfig.DEFAULT));

        metricsCollector = new MetricsCollector(context);

        ScheduledThreadPoolExecutor executor = (ScheduledThreadPoolExecutor) context.getExecutorFactory()
                .create(ExecutorFactory.MONITOR, context.getEnvironment());

        executor.scheduleAtFixedRate(this::scheduleMonitor, 5, 10, TimeUnit.SECONDS);

        bind(context);
    }

    private void bind(PiccoloContext context) {
        MetricsMonitor.monitorDisk();
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

    private void scheduleMonitor() {
        long startTime = System.currentTimeMillis();
        Map<String, Object> result = metricsCollector.monitor().getResults();
        result.forEach((tag, ret) -> {
            if (ret instanceof Map) {
                monitorResult(tag, null, (Map<String, Object>) ret);
            } else {
                LOGGER.warn("collect metric invalid type: {}, expect Map.", ret);
            }
        });
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("monitor use {} ms", System.currentTimeMillis() - startTime);
        }
    }

    private void monitorResult(String tag, String module, Map<String, Object> map) {
        map.forEach((key, val) -> {
            if (val instanceof Map) {
                monitorResult(tag, key, (Map<String, Object>) val);
            } else if (val instanceof Number) {
//                MetricsMonitor.gaugeWithStrongRef(tag, module, key, () -> (Number) val);
                MetricsMonitor.gauge(tag, module, key, (Number) val);
            } else {
                LOGGER.warn("collect metric invalid type, key:{} val: {}, expect Map or Number.", key, val);
            }
        });
    }
}
