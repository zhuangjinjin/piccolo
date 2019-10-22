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

import io.github.ukuz.piccolo.api.common.threadpool.ExecutorFactory;
import io.github.ukuz.piccolo.api.config.Environment;
import io.github.ukuz.piccolo.api.external.common.Assert;
import io.micrometer.core.instrument.Metrics;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;

/**
 * @author ukuz90
 */
public class MonitorExecutorFactory implements ExecutorFactory {

    private final ConcurrentMap<String, Executor> executors = new ConcurrentHashMap<>();
    private final ExecutorFactory delegate;

    public MonitorExecutorFactory(ExecutorFactory delegate) {
        Assert.notNull(delegate, "delegate must not be null");
        this.delegate = delegate;
    }

    @Override
    public Executor create(String name, Environment environment) {
        return executors.computeIfAbsent(name, k -> InternalExecutorServiceMetrics.monitor(Metrics.globalRegistry, delegate.create(k, environment), k));
    }

    public void monitor(String name, Executor executor) {
        executors.put(name, InternalExecutorServiceMetrics.monitor(Metrics.globalRegistry, executor, name));
    }

    public Map<String, Executor> getAllThreadPool() {
        return Collections.unmodifiableMap(executors);
    }

}
