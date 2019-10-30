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

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.binder.BaseUnits;
import io.micrometer.core.instrument.binder.MeterBinder;
import io.micrometer.core.instrument.binder.jvm.ExecutorServiceMetrics;
import io.netty.channel.EventLoopGroup;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.SingleThreadEventExecutor;

import java.util.Collections;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @author ukuz90
 */
public class InternalExecutorServiceMetrics implements MeterBinder {

    private final ExecutorService executorService;
    private final Iterable<Tag> tags;

    public InternalExecutorServiceMetrics(ExecutorService executorService, String executorServiceName, Iterable<Tag> tags) {
        this.executorService = executorService;
        this.tags = Tags.concat(tags, "name", executorServiceName);
    }

    public static Executor monitor(MeterRegistry registry, Executor executor, String executorName) {
        return monitor(registry, executor, executorName, Collections.emptyList());
    }

    public static Executor monitor(MeterRegistry registry, Executor executor, String executorName, Iterable<Tag> tags) {
        if (executor instanceof EventLoopGroup || executor instanceof ScheduledExecutorService) {
            return monitor(registry, (ExecutorService) executor, executorName, tags);
        } else {
            return ExecutorServiceMetrics.monitor(registry, executor, executorName, tags);
        }
    }

    private static Executor monitor(MeterRegistry registry, ExecutorService executor, String executorName, Iterable<Tag> tags) {
        if (executor instanceof EventLoopGroup) {
            new InternalExecutorServiceMetrics(executor, executorName, tags).bindTo(registry);
            return executor;
        } else if (executor instanceof ScheduledExecutorService) {
            new ExecutorServiceMetrics(executor, executorName, tags).bindTo(registry);
            return executor;
        }
        return executor;
    }

    @Override
    public void bindTo(MeterRegistry registry) {
        if (executorService == null) {
            return;
        }

        if (executorService instanceof EventLoopGroup) {
            monitor(registry, (EventLoopGroup) executorService);
        }
    }

    private void monitor(MeterRegistry registry, EventLoopGroup executors) {
        Gauge.builder("netty.executor.pool.size", executors, this::getPoolSize)
                .tags(tags)
                .baseUnit(BaseUnits.THREADS)
                .register(registry);

        Gauge.builder("netty.executor.queue.size", executors, this::getQueueSize)
                .tags(tags)
                .baseUnit(BaseUnits.TASKS)
                .register(registry);

        Gauge.builder("netty.executor.pool.active", executors, this::getActiveCount)
                .tags(tags)
                .baseUnit(BaseUnits.THREADS)
                .register(registry);

    }

    private int getPoolSize(EventLoopGroup executors) {
        int poolSize = 0;
        for (EventExecutor executor : executors) {
            if (executor instanceof SingleThreadEventExecutor) {
                poolSize++;
            }
        }
        return poolSize;
    }

    private int getActiveCount(EventLoopGroup executors) {
        int activeCount = 0;
        for (EventExecutor executor : executors) {
            if (executor instanceof SingleThreadEventExecutor) {
                if (((SingleThreadEventExecutor) executor).threadProperties().state() == Thread.State.RUNNABLE) {
                    activeCount ++;
                }
            }
        }
        return activeCount;
    }

    private int getQueueSize(EventLoopGroup executors) {
        int queueSize = 0;
        for (EventExecutor executor : executors) {
            if (executor instanceof SingleThreadEventExecutor) {
                queueSize += ((SingleThreadEventExecutor) executor).pendingTasks();
            }
        }
        return queueSize;
    }

}
