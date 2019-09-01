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

import com.google.common.collect.Maps;
import io.github.ukuz.piccolo.api.mq.MQMessageReceiver;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentMap;

/**
 * @author ukuz90
 */
public class KafkaConsumerWorker implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaConsumerWorker.class);
    private final Map<String, Object> properties;
    private List<String> topics;
    private volatile boolean running = true;
    private final MQMessageReceiver receiver;
    private final ConcurrentMap<TopicPartition, OffsetAndMetadata> offsetMap;
    private final KafkaConsumer consumer;

    public KafkaConsumerWorker(Map<String, Object> properties, List<String> topics, MQMessageReceiver receiver) {
        this.properties = properties;
        this.topics = topics;
        this.receiver = receiver;
        this.offsetMap = Maps.newConcurrentMap();
        this.consumer = new KafkaConsumer(properties);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void run() {
        try {
            consumer.subscribe(topics, new ConsumerRebalanceListener() {
                @Override
                public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
                    commitSync(true);
                }

                @Override
                public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
                    clearOffsets();
                }
            });
            while (isRunning()) {
                ConsumerRecords<String, byte[]> records = consumer.poll(Duration.ofMillis(20));
                records.forEach(record -> {
                    LOGGER.info("topic: {} partition: {} offset: {} ", record.topic(), record.partition(), record.offset());
                    receiver.receive(record.topic(), record.value(),
                            new TopicPartition(record.topic(), record.partition()),
                            new OffsetAndMetadata(record.offset() + 1));
                });
                commitSync(false);
            }

        } catch (Exception e) {

        }
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public void commit(TopicPartition topicPartition, OffsetAndMetadata offsetAndMetadata) {
        synchronized (offsetMap) {
            OffsetAndMetadata offset = offsetMap.get(topicPartition);
            if (offset == null) {
                offsetMap.put(topicPartition, offsetAndMetadata);
            } else {
                if (offset.offset() < offsetAndMetadata.offset()) {
                    offsetMap.put(topicPartition, offsetAndMetadata);
                }
            }
        }
    }

    public void commitSync(boolean sync) {
        Map<TopicPartition, OffsetAndMetadata> unmodifiedMap;
        synchronized (offsetMap) {
            if (offsetMap.isEmpty()) {
                return;
            }

            unmodifiedMap = Collections.unmodifiableMap(new HashMap<>(offsetMap));
            offsetMap.clear();
        }

        if (sync) {
            try {
                consumer.commitSync(unmodifiedMap);
                LOGGER.info("sync commit success, offsets: {}", unmodifiedMap);
            } catch (KafkaException e) {
                LOGGER.error("sync commit error, offsets: {} cause: {}", unmodifiedMap, e);
                backupExceptionOffsets(unmodifiedMap);
            }

        } else {
            consumer.commitAsync(unmodifiedMap, (offsets, exception) -> {
                if (exception == null) {
                    LOGGER.info("async commit success, offsets: {}", offsets);
                } else {
                    LOGGER.error("async commit error, offsets: {} cause: {}", offsets, exception);
                    backupExceptionOffsets(offsets);
                }
            });
        }
    }

    private void backupExceptionOffsets(Map<TopicPartition, OffsetAndMetadata> offsets) {
        offsets.forEach(this::commit);
    }

    private void clearOffsets() {
        synchronized (offsetMap) {
            offsetMap.clear();
        }
    }

    public void destroy() {
        setRunning(false);
        commitSync(true);
        consumer.close();
    }
}
