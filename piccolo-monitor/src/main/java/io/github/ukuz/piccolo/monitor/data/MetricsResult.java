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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author ukuz90
 */
public class MetricsResult {

    private long timestamp;
    private Map<String, Object> results = new HashMap<>();

    public MetricsResult() {
        this.timestamp = System.currentTimeMillis();
    }

    public MetricsResult result(String key, Object value) {
        results.put(key, value);
        return this;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public Map<String, Object> getResults() {
        return Collections.unmodifiableMap(results);
    }

    @Override
    public String toString() {
        return "MetricsResult{" +
                "timestamp=" + timestamp +
                ", results=" + results +
                '}';
    }
}
