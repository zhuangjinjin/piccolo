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
package io.github.ukuz.piccolo.monitor.quota.impl;

import io.github.ukuz.piccolo.monitor.quota.ThreadPoolQuota;
import io.netty.channel.EventLoopGroup;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.SingleThreadEventExecutor;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author ukuz90
 */
public class JVMThreadPool implements ThreadPoolQuota {

    @Override
    public Object monitor(Object... args) {
        return null;
    }

    private Map<String, Object> getPoolInfo(ThreadPoolExecutor executor) {
        Map<String, Object> result = new LinkedHashMap<>(5);
        result.put("corePoolSize", executor.getCorePoolSize());
        result.put("maxPoolSize", executor.getMaximumPoolSize());
        result.put("activeCount(workingThread)", executor.getActiveCount());
        result.put("poolSize(workingThread)", executor.getPoolSize());
        result.put("queueSize(workingThread)", executor.getQueue().size());
        return result;
    }

    private Map<String, Object> getPoolInfo(EventLoopGroup executors) {
        Map<String, Object> result = new LinkedHashMap<>(3);
        int activeCount = 0;
        int poolSize = 0;
        int queueSize = 0;
        for (EventExecutor e : executors) {
            poolSize++;
            if (e instanceof SingleThreadEventExecutor) {
                SingleThreadEventExecutor executor = (SingleThreadEventExecutor) e;
                queueSize += executor.pendingTasks();
                if (executor.threadProperties().state() == Thread.State.RUNNABLE) {
                    activeCount++;
                }
            }
        }
        result.put("activeCount(workingThread)", activeCount);
        result.put("poolSize(workingThread)", poolSize);
        result.put("queueSize(workingThread)", queueSize);
        return result;
    }
}
