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

import io.github.ukuz.piccolo.monitor.quota.MemoryQuota;
import io.netty.util.internal.PlatformDependent;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

/**
 * @author ukuz90
 */
public class JVMMemory implements MemoryQuota {

    private final String OLD_GEN_NAME_PATTERN = "CMS Old Gen|Tenured Gen|PS Old Gen|G1 Old Gen";
    private final String PERM_GEN_NAME_PATTERN = "Metaspace";
    private final String EDEN_NAME_PATTERN = "Par Eden Space|Eden Space|PS Eden Space|G1 Eden Space";
    private final String SURVIVOR_NAME_PATTERN = "Par Survivor Space|Survivor Space|PS Survivor Space|G1 Survivor Space";


    private MemoryMXBean memoryMXBean;
    private MemoryPoolMXBean permGenMXBean;
    private MemoryPoolMXBean oldGenMXBean;
    private MemoryPoolMXBean edenMXBean;
    private MemoryPoolMXBean survivorMXBean;

    private AtomicLong nativeMemoryUsed;
    private long nativeMemoryLimit;

    public JVMMemory() {
        memoryMXBean = ManagementFactory.getMemoryMXBean();
        List<MemoryPoolMXBean> list = ManagementFactory.getMemoryPoolMXBeans();

        permGenMXBean = find(list, Pattern.compile(PERM_GEN_NAME_PATTERN));
        oldGenMXBean = find(list, Pattern.compile(OLD_GEN_NAME_PATTERN));
        edenMXBean = find(list, Pattern.compile(EDEN_NAME_PATTERN));
        survivorMXBean = find(list, Pattern.compile(SURVIVOR_NAME_PATTERN));

        try {
            Field counterField = PlatformDependent.class.getDeclaredField("DIRECT_MEMORY_COUNTER");
            counterField.setAccessible(true);
            nativeMemoryUsed = (AtomicLong) counterField.get(PlatformDependent.class);

            Field limitField = PlatformDependent.class.getDeclaredField("DIRECT_MEMORY_LIMIT");
            limitField.setAccessible(true);
            nativeMemoryLimit = (long) limitField.get(PlatformDependent.class);
            limitField.setAccessible(false);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private static MemoryPoolMXBean find(List<MemoryPoolMXBean> list, Pattern namePattern) {
        return list.stream()
                .filter(mxBean -> namePattern.matcher(mxBean.getName()).matches())
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Can not found memoryPoolMXBean, namePattern: " + namePattern.pattern()));
    }

    @Override
    public long heapMemoryCommitted() {
        return memoryMXBean.getHeapMemoryUsage().getCommitted();
    }

    @Override
    public long heapMemoryInit() {
        return memoryMXBean.getHeapMemoryUsage().getInit();
    }

    @Override
    public long heapMemoryMax() {
        return memoryMXBean.getHeapMemoryUsage().getMax();
    }

    @Override
    public long heapMemoryUsed() {
        return memoryMXBean.getHeapMemoryUsage().getUsed();
    }

    @Override
    public long nonHeapMemoryCommitted() {
        return memoryMXBean.getNonHeapMemoryUsage().getCommitted();
    }

    @Override
    public long nonHeapMemoryInit() {
        return memoryMXBean.getNonHeapMemoryUsage().getInit();
    }

    @Override
    public long nonHeapMemoryMax() {
        return memoryMXBean.getNonHeapMemoryUsage().getMax();
    }

    @Override
    public long nonHeapMemoryUsed() {
        return memoryMXBean.getNonHeapMemoryUsage().getUsed();
    }

    @Override
    public long nativeMemoryUsed() {
        if (nativeMemoryUsed != null) {
            return nativeMemoryUsed.get();
        }
        return 0;
    }

    @Override
    public long nativeMemoryMax() {
        return nativeMemoryLimit;
    }

    @Override
    public long permGenCommitted() {
        return permGenMXBean.getUsage().getCommitted();
    }

    @Override
    public long permGenInit() {
        return permGenMXBean.getUsage().getInit();
    }

    @Override
    public long permGenMax() {
        return permGenMXBean.getUsage().getMax();
    }

    @Override
    public long permGenUsed() {
        return permGenMXBean.getUsage().getUsed();
    }

    @Override
    public long oldGenCommitted() {
        return oldGenMXBean.getUsage().getCommitted();
    }

    @Override
    public long oldGenInit() {
        return oldGenMXBean.getUsage().getInit();
    }

    @Override
    public long oldGenMax() {
        return oldGenMXBean.getUsage().getMax();
    }

    @Override
    public long oldGenUsed() {
        return oldGenMXBean.getUsage().getUsed();
    }

    @Override
    public long edenSpaceCommitted() {
        return edenMXBean.getUsage().getCommitted();
    }

    @Override
    public long edenSpaceInit() {
        return edenMXBean.getUsage().getInit();
    }

    @Override
    public long edenSpaceMax() {
        return edenMXBean.getUsage().getMax();
    }

    @Override
    public long edenSpaceUsed() {
        return edenMXBean.getUsage().getUsed();
    }

    @Override
    public long survivorCommitted() {
        return survivorMXBean.getUsage().getCommitted();
    }

    @Override
    public long survivorInit() {
        return survivorMXBean.getUsage().getInit();
    }

    @Override
    public long survivorMax() {
        return survivorMXBean.getUsage().getMax();
    }

    @Override
    public long survivorUsed() {
        return survivorMXBean.getUsage().getUsed();
    }

    @Override
    public Object monitor(Object... args) {
        Map<String, Object> result = new LinkedHashMap<>(26);
        result.put("heapMemoryCommitted", heapMemoryCommitted());
        result.put("heapMemoryInit", heapMemoryInit());
        result.put("heapMemoryMax", heapMemoryMax());
        result.put("heapMemoryUsed", heapMemoryUsed());

        result.put("nonHeapMemoryCommitted", nonHeapMemoryCommitted());
        result.put("nonHeapMemoryInit", nonHeapMemoryInit());
        result.put("nonHeapMemoryMax", nonHeapMemoryMax());
        result.put("nonHeapMemoryUsed", nonHeapMemoryUsed());

        result.put("nativeMemoryUsed", nativeMemoryUsed());
        result.put("nativeMemoryMax", nativeMemoryMax());

        result.put("permGenCommitted", permGenCommitted());
        result.put("permGenInit", permGenInit());
        result.put("permGenMax", permGenMax());
        result.put("permGenUsed", permGenUsed());

        result.put("oldGenCommitted", oldGenCommitted());
        result.put("oldGenInit", oldGenInit());
        result.put("oldGenMax", oldGenMax());
        result.put("oldGenUsed", oldGenUsed());

        result.put("edenSpaceCommitted", edenSpaceCommitted());
        result.put("edenSpaceInit", edenSpaceInit());
        result.put("edenSpaceMax", edenSpaceMax());
        result.put("edenSpaceUsed", edenSpaceUsed());

        result.put("survivorCommitted", survivorCommitted());
        result.put("survivorInit", survivorInit());
        result.put("survivorMax", survivorMax());
        result.put("survivorUsed", survivorUsed());
        return result;
    }

}
