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
package io.github.ukuz.piccolo.common.threadpool;

import io.github.ukuz.piccolo.api.common.threadpool.ExecutorFactory;
import io.github.ukuz.piccolo.api.config.Environment;
import io.github.ukuz.piccolo.api.external.common.Assert;
import io.github.ukuz.piccolo.common.properties.ThreadPoolProperties;
import io.github.ukuz.piccolo.common.thread.ThreadNames;

import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author ukuz90
 */
public  class AbstractExecutorFactory implements ExecutorFactory {

    protected Executor createExecutor(ThreadPoolConfig config) {
        Assert.notNull(config, "config must not be null");
        return new ThreadPoolExecutor(
                config.getCoreSize(),
                config.getMaxSize(),
                config.getKeepAliveSeconds(),
                TimeUnit.SECONDS,
                config.getTaskQueue(),
                config.getThreadFactory(),
                config.getExecutionHandler()
        );
    }

    protected Executor createScheduledExecutor(ThreadPoolConfig config) {
        Assert.notNull(config, "config must not be null");
        return new ScheduledThreadPoolExecutor(
                config.getCoreSize(),
                config.getThreadFactory(),
                config.getExecutionHandler()
        );
    }

    @Override
    public Executor create(String name, Environment environment) {
        ThreadPoolProperties tp = environment.getProperties(ThreadPoolProperties.class);
        ThreadPoolConfig config = null;
        switch (name) {
            case EVENT_BUS:
                config = ThreadPoolConfig.builder()
                        .name(ThreadNames.T_EVENT_BUS)
                        .coreSize(tp.getEventBus().getCoreSize())
                        .maxSize(tp.getEventBus().getMaxSize())
                        .queueCapacity(tp.getEventBus().getQueueSize())
                        .keepAliveSeconds(tp.getEventBus().getKeepAliveSeconds())
                        .build();

                return createExecutor(config);
            default:
                throw new IllegalArgumentException("invalid name: " + name);
        }
    }
}
