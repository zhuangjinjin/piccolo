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

import io.github.ukuz.piccolo.monitor.quota.ThreadQuota;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author ukuz90
 */
public class JVMThread implements ThreadQuota {

    private ThreadMXBean threadMXBean;

    public JVMThread() {
        this.threadMXBean = ManagementFactory.getThreadMXBean();
    }

    @Override
    public int daemonThreadCount() {
        return threadMXBean.getDaemonThreadCount();
    }

    @Override
    public int threadCount() {
        return threadMXBean.getThreadCount();
    }

    @Override
    public long totalStartedThreadCount() {
        return threadMXBean.getTotalStartedThreadCount();
    }

    @Override
    public int deadlockedThreadCount() {
        try {
            long[] deadlockedThreadIds = threadMXBean.findDeadlockedThreads();
            if (deadlockedThreadIds != null) {
                return deadlockedThreadIds.length;
            }
        } catch (Exception e) {
            return 0;
        }
        return 0;
    }

    @Override
    public Object monitor(Object... args) {
        Map<String, Object> result = new LinkedHashMap<>(4);
        result.put("daemonThreadCount", daemonThreadCount());
        result.put("threadCount", threadCount());
        result.put("totalStartedThreadCount", totalStartedThreadCount());
        result.put("deadlockedThreadCount", deadlockedThreadCount());
        return result;
    }
}
