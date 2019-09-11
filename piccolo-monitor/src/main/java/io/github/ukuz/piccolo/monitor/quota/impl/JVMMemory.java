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

import io.github.ukuz.piccolo.api.common.MemorySize;
import io.github.ukuz.piccolo.monitor.quota.MemoryQuota;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

    public JVMMemory() {
        memoryMXBean = ManagementFactory.getMemoryMXBean();
        List<MemoryPoolMXBean> list = ManagementFactory.getMemoryPoolMXBeans();

        permGenMXBean = find(list, Pattern.compile(PERM_GEN_NAME_PATTERN));
        oldGenMXBean = find(list, Pattern.compile(OLD_GEN_NAME_PATTERN));
        edenMXBean = find(list, Pattern.compile(EDEN_NAME_PATTERN));
        survivorMXBean = find(list, Pattern.compile(SURVIVOR_NAME_PATTERN));
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
        Map<String, Object> result = new LinkedHashMap<>(24);
        result.put("heapMemoryCommitted", MemorySize.prettyMemorySize(heapMemoryCommitted()));
        result.put("heapMemoryInit", MemorySize.prettyMemorySize(heapMemoryInit()));
        result.put("heapMemoryMax", MemorySize.prettyMemorySize(heapMemoryMax()));
        result.put("heapMemoryUsed", MemorySize.prettyMemorySize(heapMemoryUsed()));

        result.put("nonHeapMemoryCommitted", MemorySize.prettyMemorySize(nonHeapMemoryCommitted()));
        result.put("nonHeapMemoryInit", MemorySize.prettyMemorySize(nonHeapMemoryInit()));
        result.put("nonHeapMemoryMax", MemorySize.prettyMemorySize(nonHeapMemoryMax()));
        result.put("nonHeapMemoryUsed", MemorySize.prettyMemorySize(nonHeapMemoryUsed()));

        result.put("permGenCommitted", MemorySize.prettyMemorySize(permGenCommitted()));
        result.put("permGenInit", MemorySize.prettyMemorySize(permGenInit()));
        result.put("permGenMax", MemorySize.prettyMemorySize(permGenMax()));
        result.put("permGenUsed", MemorySize.prettyMemorySize(permGenUsed()));

        result.put("oldGenCommitted", MemorySize.prettyMemorySize(oldGenCommitted()));
        result.put("oldGenInit", MemorySize.prettyMemorySize(oldGenInit()));
        result.put("oldGenMax", MemorySize.prettyMemorySize(oldGenMax()));
        result.put("oldGenUsed", MemorySize.prettyMemorySize(oldGenUsed()));

        result.put("edenSpaceCommitted", MemorySize.prettyMemorySize(edenSpaceCommitted()));
        result.put("edenSpaceInit", MemorySize.prettyMemorySize(edenSpaceInit()));
        result.put("edenSpaceMax", MemorySize.prettyMemorySize(edenSpaceMax()));
        result.put("edenSpaceUsed", MemorySize.prettyMemorySize(edenSpaceUsed()));

        result.put("survivorCommitted", MemorySize.prettyMemorySize(survivorCommitted()));
        result.put("survivorInit", MemorySize.prettyMemorySize(survivorInit()));
        result.put("survivorMax", MemorySize.prettyMemorySize(survivorMax()));
        result.put("survivorUsed", MemorySize.prettyMemorySize(survivorUsed()));
        return result;
    }

    public static void main(String[] args) throws InterruptedException {
        JVMMemory memory = new JVMMemory();
        while (true) {
            System.out.println(memory.monitor());
            Thread.sleep(3000);
        }

    }
}
