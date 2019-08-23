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
package io.github.ukuz.piccolo.api.mq;

import lombok.Data;

/**
 * @author ukuz90
 */
@Data
public class MQTopic {

    private static final String NS = "piccolo";

    private final String topic;
    private final int numPartitions;
    private final short replicationFactor;

    public MQTopic(String topic) {
        this(topic, 1);
    }

    public MQTopic(String topic, int numPartitions) {
        this(topic, numPartitions, (short) 1);
    }

    public MQTopic(String topic, int numPartitions, short replicationFactor) {
        this.topic = getTopic(topic);
        this.numPartitions = numPartitions;
        this.replicationFactor = replicationFactor;
    }

    public static String getTopic(String topic) {
        if (topic.indexOf(NS) != 0) {
            topic = NS + "." + topic;
        }
        return topic;
    }

}
