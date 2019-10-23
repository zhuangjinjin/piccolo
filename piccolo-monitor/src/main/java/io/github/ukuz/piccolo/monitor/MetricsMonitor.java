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

import io.github.ukuz.piccolo.api.common.utils.StringUtils;
import io.github.ukuz.piccolo.api.external.common.Assert;
import io.github.ukuz.piccolo.monitor.quota.GCQuota;
import io.micrometer.core.instrument.*;
import io.micrometer.core.instrument.binder.jvm.DiskSpaceMetrics;
import io.netty.util.internal.PlatformDependent;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * @author ukuz90
 */
public class MetricsMonitor {

    private static final String REQUEST_CNT_TAG = "piccolo_recv_count";
    private static final String REQUEST_BYTES_TAG = "piccolo_recv_bytes";
    private static final String RESPONSE_CNT_TAG = "piccolo_send_count";
    private static final String RESPONSE_BYTES_TAG = "piccolo_send_bytes";

    public static void gauge(String tag, String module, String name, Number value) {
        if (StringUtils.hasText(module)) {
            gauge(tag, value, "module", module, "name", name);
        } else {
            gauge(tag, value, "name", name);
        }
    }

    public static void gaugeWithStrongRef(String tag, String module, String name, Supplier<Number> supplier) {
        List<Tag> labelSets;
        if (StringUtils.hasText(module)) {
            labelSets = wrapLabelSet("module", module, "name", name);
        } else {
            labelSets = wrapLabelSet("name", name);
        }
        Gauge.builder(tag, supplier).tags(labelSets).register(Metrics.globalRegistry);
    }

    private static Counter counter(String metricName, String ...label) {
        return Metrics.counter(metricName, wrapLabelSet(label));
    }

    private static void gauge(String metricName, Number value, String ...label) {
        Metrics.gauge(metricName, wrapLabelSet(label), value);
    }

    private static List<Tag> wrapLabelSet(String ...label) {
        if (label.length > 0) {
            Assert.isTrue(label.length % 2 == 0, "invalid arguments");
        }
        List<Tag> labelSet = new ArrayList<>(label.length / 2);
        for (int i = 0; i < label.length; i += 2) {
            labelSet.add(new ImmutableTag(label[i], label[i+1]));
        }
        return labelSet;
    }

//    public static final Counter getWebSocketQuestCount() {
//        return counter(MONITOR_TAG, "module", "net", "name", "websocketQuestCount");
//    }
//
//    public static final Counter getWebSocketBytesCount() {
//        return counter(MONITOR_TAG, "module", "net", "name", "websocketBytesCount");
//    }

    public static final Counter getQuestCount(String name) {
        return counter(REQUEST_CNT_TAG, "name", name);
    }

    public static final Counter getQuestBytes(String name) {
        return counter(REQUEST_BYTES_TAG, "name", name);
    }

    public static final Counter getResponseCount(String name) {
        return counter(RESPONSE_CNT_TAG, "name", name);
    }

    public static final Counter getResponseBytes(String name) {
        return counter(RESPONSE_BYTES_TAG, "name", name);
    }

    public static final void monitorDisk() {
        if (!PlatformDependent.isWindows()) {
            new DiskSpaceMetrics(new File("/")).bindTo(Metrics.globalRegistry);
        }
    }

    public static final void monitorGC(GCQuota gcQuota) {
        new JVMGCMetrics(gcQuota).bindTo(Metrics.globalRegistry);
    }

}
