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

import io.github.ukuz.piccolo.monitor.quota.InfoQuota;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author ukuz90
 */
public class JVMInfo implements InfoQuota {

    private RuntimeMXBean runtimeMXBean;

    private OperatingSystemMXBean operatingSystemMXBean;

    private String pid;

    public JVMInfo() {
        runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
    }

    @Override
    public String pid() {
        if (pid == null) {
            pid = runtimeMXBean.getName().split("@")[0];
        }
        return pid;
    }

    @Override
    public double load() {
        return operatingSystemMXBean.getSystemLoadAverage();
    }

    @Override
    public Object monitor(Object... args) {
        Map<String, Object> result = new LinkedHashMap<>(5);
        result.put("pid", Long.valueOf(pid()));
        result.put("load", load());
        result.put("totalMemory", Runtime.getRuntime().totalMemory());
        result.put("freeMemory", Runtime.getRuntime().freeMemory());
        result.put("maxMemory", Runtime.getRuntime().maxMemory());
        return result;
    }
}
