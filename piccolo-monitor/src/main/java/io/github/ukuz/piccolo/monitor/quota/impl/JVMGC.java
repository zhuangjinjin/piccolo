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

import io.github.ukuz.piccolo.monitor.quota.GCQuota;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author ukuz90
 */
public class JVMGC implements GCQuota {

    /**
     * -XX:+UseParallelGC
     * -XX:+UseG1GC
     * -XX:+UseSerialGC
     * -XX:+UseConcSweepGC
     * -XX:+UseParNewGC
     * -XX:+UseParallelOldGC
     *
     */
    private static final String FULL_GC_NAME_PATTERN = "PS MarkSweep|G1 Old Generation|MarkSweepCompact|ConcurrentMarkSweep";
    private static final String YOUNG_GC_NAME_PATTERN = "PS Scavenge|G1 Young Generation|Copy|ParNew";

    private GarbageCollectorMXBean fullGCMXBean;
    private GarbageCollectorMXBean youngGCMXBean;

    private long lastYoungGcCollectionCount = -1;
    private long lastYoungGcCollectionTime = -1;
    private long lastFullGcCollectionCount = -1;
    private long lastFullGcCollectionTime = -1;

    public JVMGC() {
        List<GarbageCollectorMXBean> list = ManagementFactory.getGarbageCollectorMXBeans();

        fullGCMXBean = find(list, Pattern.compile(FULL_GC_NAME_PATTERN));
        youngGCMXBean = find(list, Pattern.compile(YOUNG_GC_NAME_PATTERN));
    }

    private static GarbageCollectorMXBean find(List<GarbageCollectorMXBean> list, Pattern namePattern) {
        return list.stream()
                .filter(mxBean -> namePattern.matcher(mxBean.getName()).matches())
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Can not found garbageCollectorMXBean, namePattern: " + namePattern.pattern()));
    }

    @Override
    public long youngGcCollectionCount() {
        return youngGCMXBean.getCollectionCount();
    }

    @Override
    public long youngGcCollectionTime() {
        return youngGCMXBean.getCollectionTime();
    }

    @Override
    public long fullGcCollectionCount() {
        return fullGCMXBean.getCollectionCount();
    }

    @Override
    public long fullGcCollectionTime() {
        return fullGCMXBean.getCollectionTime();
    }

    @Override
    public long spanYoungGcCollectionCount() {
        long curr = youngGcCollectionCount();
        if (lastYoungGcCollectionCount == -1) {
            lastYoungGcCollectionCount = curr;
            return 0;
        } else {
            long result = curr - lastYoungGcCollectionCount;
            lastYoungGcCollectionCount = curr;
            return result;
        }
    }

    @Override
    public long spanYoungGcCollectionTime() {
        long curr = youngGcCollectionTime();
        if (lastYoungGcCollectionTime == -1) {
            lastYoungGcCollectionTime = curr;
            return 0;
        } else {
            long result = curr - lastYoungGcCollectionTime;
            lastYoungGcCollectionTime = curr;
            return result;
        }
    }

    @Override
    public long spanFullGcCollectionCount() {
        long curr = fullGcCollectionCount();
        if (lastFullGcCollectionCount == -1) {
            lastFullGcCollectionCount = curr;
            return 0;
        } else {
            long result = curr - lastFullGcCollectionCount;
            lastFullGcCollectionCount = curr;
            return result;
        }
    }

    @Override
    public long spanFullGcCollectionTime() {
        long curr = fullGcCollectionTime();
        if (lastFullGcCollectionTime == -1) {
            lastFullGcCollectionTime = curr;
            return 0;
        } else {
            long result = curr - lastFullGcCollectionTime;
            lastFullGcCollectionTime = curr;
            return result;
        }
    }

    @Override
    public Object monitor(Object... args) {
        Map<String, Object> result = new LinkedHashMap<>(8);

        result.put("youngGcCollectionCount", youngGcCollectionCount());
        result.put("youngGcCollectionTime", youngGcCollectionTime());
        result.put("fullGcCollectionCount", fullGcCollectionCount());
        result.put("fullGcCollectionTime", fullGcCollectionTime());

        result.put("spanYoungGcCollectionCount", spanYoungGcCollectionCount());
        result.put("spanYoungGcCollectionTime", spanYoungGcCollectionTime());
        result.put("spanFullGcCollectionCount", spanFullGcCollectionCount());
        result.put("spanFullGcCollectionTime", spanFullGcCollectionTime());

        return result;
    }

}
