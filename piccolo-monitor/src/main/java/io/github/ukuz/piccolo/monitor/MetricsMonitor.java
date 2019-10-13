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

import io.micrometer.core.instrument.ImmutableTag;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Tag;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author ukuz90
 */
public class MetricsMonitor {

    private static final String TAG = "piccolo_monitor";

    private static final AtomicLong webSocketQuestCount = new AtomicLong();
    private static final AtomicLong webSocketBytesCount = new AtomicLong();

    static {
        addTag(TAG, "net","webSocketQuestCount", webSocketQuestCount);
        addTag(TAG, "net", "webSocketBytesCount", webSocketBytesCount);
    }

    private static void addTag(String tag, String module, String name, Number value) {
        List<Tag> tags = new ArrayList<>();
        tags.add(new ImmutableTag("module", module));
        tags.add(new ImmutableTag("name", name));
        Metrics.gauge(tag, tags, value);
    }

    public static final AtomicLong getWebSocketQuestCount() {
        return webSocketQuestCount;
    }

    public static final AtomicLong getWebSocketBytesCount() {
        return webSocketBytesCount;
    }

}
