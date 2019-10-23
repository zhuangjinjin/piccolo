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

import io.github.ukuz.piccolo.monitor.quota.GCQuota;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;

/**
 * @author ukuz90
 */
public class JVMGCMetrics implements MeterBinder {

    private final GCQuota gcQuota;

    private static final String COUNT = "count";
    private static final String TIME = "time";

    public JVMGCMetrics(GCQuota gcQuota) {
        this.gcQuota = gcQuota;
    }

    @Override
    public void bindTo(MeterRegistry registry) {
        Gauge.builder("jvm.gc.collection.count", gcQuota, GCQuota::youngGcCollectionCount)
                .description("")
                .baseUnit(COUNT)
                .tag("name", "young gc")
                .register(registry);

        Gauge.builder("jvm.gc.collection.count", gcQuota, GCQuota::fullGcCollectionCount)
                .description("")
                .baseUnit(COUNT)
                .tag("name", "full gc")
                .register(registry);

        Gauge.builder("jvm.gc.collection.time", gcQuota, GCQuota::youngGcCollectionTime)
                .description("")
                .baseUnit(TIME)
                .tag("name", "young gc")
                .register(registry);

        Gauge.builder("jvm.gc.collection.time", gcQuota, GCQuota::fullGcCollectionTime)
                .description("")
                .baseUnit(TIME)
                .tag("name", "full gc")
                .register(registry);
    }
}
