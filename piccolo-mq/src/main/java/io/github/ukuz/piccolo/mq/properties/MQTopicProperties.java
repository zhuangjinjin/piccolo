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
package io.github.ukuz.piccolo.mq.properties;

import io.github.ukuz.piccolo.api.config.ConfigurationProperties;
import io.github.ukuz.piccolo.api.config.Properties;
import io.github.ukuz.piccolo.api.mq.MQTopic;
import static io.github.ukuz.piccolo.mq.kafka.Topics.DISPATCH_MESSAGE;
import static io.github.ukuz.piccolo.mq.kafka.Topics.ONLINE_MESSAGE;
import static io.github.ukuz.piccolo.mq.kafka.Topics.OFFLINE_MESSAGE;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author ukuz90
 */
@ConfigurationProperties(prefix = "piccolo.mq-topic")
@Data
public class MQTopicProperties implements Properties {

    private TopicNestedProperties dispatch;
    private TopicNestedProperties online;
    private TopicNestedProperties offline;

    @Data
    public class TopicNestedProperties implements Properties {
        private int numPartitions;
        private short replicationFactor;

        private MQTopic build(String topic) {
            int numPartitions = Math.max(1, this.numPartitions);
            short replicationFactor = (short) Math.max(1, this.replicationFactor);
            return new MQTopic(topic, numPartitions, replicationFactor);
        }
    }

    public List<MQTopic> getAllTopics() {
        List<MQTopic> list = new ArrayList<>();
        Optional.ofNullable(dispatch.build(DISPATCH_MESSAGE.getTopic())).ifPresent(list::add);
        Optional.ofNullable(online.build(ONLINE_MESSAGE.getTopic())).ifPresent(list::add);
        Optional.ofNullable(offline.build(OFFLINE_MESSAGE.getTopic())).ifPresent(list::add);
        return list;
    }

}
