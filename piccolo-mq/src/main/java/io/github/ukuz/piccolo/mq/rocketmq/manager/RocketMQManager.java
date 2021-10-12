/*
 * Copyright 2021 ukuz90
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
package io.github.ukuz.piccolo.mq.rocketmq.manager;

import io.github.ukuz.piccolo.api.PiccoloContext;
import io.github.ukuz.piccolo.api.common.threadpool.ExecutorFactory;
import io.github.ukuz.piccolo.api.external.common.Assert;
import io.github.ukuz.piccolo.api.mq.MQMessageReceiver;
import io.github.ukuz.piccolo.api.mq.MQTopic;
import io.github.ukuz.piccolo.mq.properties.MQTopicProperties;
import io.github.ukuz.piccolo.mq.rocketmq.RocketMqMessage;
import io.github.ukuz.piccolo.mq.rocketmq.consumer.RocketMQConsumerWorker;
import io.github.ukuz.piccolo.mq.rocketmq.producer.RocketMQProducerSender;
import io.github.ukuz.piccolo.mq.rocketmq.properties.RocketMQProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

/**
 * @author ukuz90
 */
public class RocketMQManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(RocketMQManager.class);

    private Executor executor;
    private PiccoloContext context;
    private RocketMQProperties rocketMQProperties;
    private RocketMQProducerSender sender;
    private ConcurrentMap<String, RocketMQConsumerWorker> consumers;

    public RocketMQManager(PiccoloContext context) {
        Assert.notNull(context, "context must not be null");
        this.context = context;
        this.consumers = new ConcurrentHashMap<>();
    }

    public void init() {
        executor = context.getExecutorFactory().create(ExecutorFactory.MQ, context.getEnvironment());
        rocketMQProperties = context.getProperties(RocketMQProperties.class);
        MQTopicProperties topicProperties = context.getProperties(MQTopicProperties.class);
        sender = new RocketMQProducerSender(rocketMQProperties.getProducer());
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

    public void subscribe(String topic, MQMessageReceiver<byte[]> receiver) {
        List<String> topics = Arrays.asList(topic.split(","));
        List<String> newTopics = topics.stream()
                .map(MQTopic::getTopic)
                .collect(Collectors.toList());
        RocketMQConsumerWorker consumer =
            new RocketMQConsumerWorker(rocketMQProperties.getConsumer(), newTopics, receiver);
        newTopics.forEach(t -> consumers.computeIfAbsent(t, k -> consumer));
        executor.execute(consumer);
    }

    public void publish(String topic, String key, Object message) {
        sender.send(topic, key, (byte[])message, null);
    }

    public void addTopicIfNeeded(MQTopic topic) {
//        admin.addTopicIfNeeded(topic);
    }

    public void commitOffset(RocketMqMessage mqMessage) {
    }
}
