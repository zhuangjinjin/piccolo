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
package io.github.ukuz.piccolo.mq.kafka.consumer;

import io.github.ukuz.piccolo.api.mq.MQMessageReceiver;
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author ukuz90
 */
public class KafkaConsumerWorker implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaConsumerWorker.class);
    private final Map<String, Object> properties;
    private List<String> topics;
    private volatile boolean running = true;
    private final MQMessageReceiver receiver;

    public KafkaConsumerWorker(Map<String, Object> properties, List<String> topics, MQMessageReceiver receiver) {
        this.properties = properties;
        this.topics = topics;
        this.receiver = receiver;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void run() {
        try(KafkaConsumer consumer = new KafkaConsumer(properties)) {
            try {
                consumer.subscribe(topics, new ConsumerRebalanceListener() {
                    @Override
                    public void onPartitionsRevoked(Collection<TopicPartition> partitions) {

                    }

                    @Override
                    public void onPartitionsAssigned(Collection<TopicPartition> partitions) {

                    }
                });
                while (isRunning()) {
                    ConsumerRecords<String, byte[]> records = consumer.poll(Duration.ofMillis(1000));
                    records.forEach(record -> {
                        receiver.receive(record.topic(), record.value());
                    });

                }

            } catch (Exception e) {

            }

        }
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }
}
