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
package io.github.ukuz.piccolo.mq.kafka.manager;

import io.github.ukuz.piccolo.api.PiccoloContext;
import io.github.ukuz.piccolo.api.common.threadpool.ExecutorFactory;
import io.github.ukuz.piccolo.api.external.common.Assert;
import io.github.ukuz.piccolo.api.mq.MQMessageReceiver;
import io.github.ukuz.piccolo.api.mq.MQTopic;
import io.github.ukuz.piccolo.mq.kafka.KafkaAdmin;
import io.github.ukuz.piccolo.mq.kafka.consumer.KafkaConsumerWorker;
import io.github.ukuz.piccolo.mq.kafka.producer.KafkaProducerSender;
import io.github.ukuz.piccolo.mq.kafka.properties.KafkaProperties;
import io.github.ukuz.piccolo.mq.properties.MQTopicProperties;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

/**
 * @author ukuz90
 */
public class KafkaManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaManager.class);

    private Executor executor;
    private PiccoloContext context;
    private KafkaProperties kafkaProperties;
    private KafkaProducerSender sender;
    private KafkaAdmin admin;
    private ConcurrentMap<String, KafkaConsumerWorker> consumers;

    public KafkaManager(PiccoloContext context) {
        Assert.notNull(context, "context must not be null");
        this.context = context;
        this.consumers = new ConcurrentHashMap<>();
    }

    public void init() {
        executor = context.getExecutorFactory().create(ExecutorFactory.MQ, context.getEnvironment());
        kafkaProperties = context.getProperties(KafkaProperties.class);
        MQTopicProperties topicProperties = context.getProperties(MQTopicProperties.class);
        Map<String, Object> producerProps = kafkaProperties.buildProducerProperties();
        Map<String, Object> adminClientProps = kafkaProperties.buildAdminClientProperties();
        sender = new KafkaProducerSender(producerProps);
        sender.init();
        admin = new KafkaAdmin(topicProperties, adminClientProps);
        admin.init();
    }

    public void destroy() {
        if (sender != null) {
            sender.destroy();
        }
        if (executor instanceof ExecutorService) {
            ((ExecutorService) executor).shutdown();
        }
        consumers.forEach((k, consumer) -> consumer.destroy());
    }

    public void subscribe(String topic, MQMessageReceiver receiver) {
        List<String> topics = Arrays.asList(topic.split(","));
        List<String> newTopics = topics.stream()
                .map(MQTopic::getTopic)
                .collect(Collectors.toList());
        Map<String, Object> properties = kafkaProperties.buildConsumerProperties();
        KafkaConsumerWorker consumer = new KafkaConsumerWorker(properties, newTopics, receiver);
        newTopics.forEach(t -> consumers.computeIfAbsent(t, k -> consumer));
        executor.execute(consumer);
    }

    public void publish(String topic, String key, Object message) {
        sender.send(MQTopic.getTopic(topic), key, (byte[])message, null);
    }

    public void addTopicIfNeeded(MQTopic topic) {
        admin.addTopicIfNeeded(topic);
    }

    public void commitOffset(TopicPartition topicPartition, OffsetAndMetadata offsetAndMetadata) {
        Assert.notNull(topicPartition, "topicPartition must not be null");
        Assert.notNull(offsetAndMetadata, "offsetAndMetadata must not be null");
        String topic = topicPartition.topic();
        KafkaConsumerWorker consumer = consumers.get(topic);
        if (consumer == null) {
            LOGGER.error("can not found topic: {}'s consumer", topic);
        } else {
            LOGGER.info("commitOffset topicPartition: {} offsetAndMetadata: {}", topicPartition, offsetAndMetadata);
            consumer.commit(topicPartition, offsetAndMetadata);
        }
    }
}
