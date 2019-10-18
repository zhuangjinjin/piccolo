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
package io.github.ukuz.piccolo.monitor.data;

import io.github.ukuz.piccolo.api.PiccoloContext;
import io.github.ukuz.piccolo.monitor.quota.*;
import io.github.ukuz.piccolo.monitor.quota.impl.*;

/**
 * @author ukuz90
 */
public class MetricsCollector {

    private GCQuota quota;
    private ThreadPoolQuota threadPoolQuota;
    private MemoryQuota memoryQuota;
    private ThreadQuota threadQuota;
    private InfoQuota infoQuota;

    public MetricsCollector(PiccoloContext context) {
        this.quota = new JVMGC();
        this.threadPoolQuota = new JVMThreadPool(context);
        this.memoryQuota = new JVMMemory();
        this.threadQuota = new JVMThread();
        this.infoQuota = new JVMInfo();
    }

    public MetricsResult monitor() {
        return new MetricsResult()
                .result("piccolo_jvm_gc", quota.monitor())
                .result("piccolo_jvm_threadpool", threadPoolQuota.monitor())
                .result("piccolo_jvm_thread", threadQuota.monitor())
                .result("piccolo_jvm_memory", memoryQuota.monitor())
                .result("piccolo_info", infoQuota.monitor())
                ;
    }
}
