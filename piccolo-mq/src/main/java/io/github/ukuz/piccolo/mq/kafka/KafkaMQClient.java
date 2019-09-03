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
package io.github.ukuz.piccolo.mq.kafka;

import io.github.ukuz.piccolo.api.PiccoloContext;
import io.github.ukuz.piccolo.api.mq.MQClient;
import io.github.ukuz.piccolo.api.mq.MQMessage;
import io.github.ukuz.piccolo.api.mq.MQMessageReceiver;
import io.github.ukuz.piccolo.api.mq.MQTopic;
import io.github.ukuz.piccolo.api.service.AbstractService;
import io.github.ukuz.piccolo.api.service.ServiceException;
import io.github.ukuz.piccolo.mq.kafka.manager.KafkaManager;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

/**
 * @author ukuz90
 */
public class KafkaMQClient extends AbstractService implements MQClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaMQClient.class);
    private KafkaManager kafkaManager;

    @Override
    public void init(PiccoloContext context) throws ServiceException {
        kafkaManager = new KafkaManager(context);
        kafkaManager.init();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected CompletableFuture<Boolean> doStartAsync() {
        CompletableFuture future = new CompletableFuture();
        future.complete(true);
        return future;
    }

    @Override
    public void destroy() throws ServiceException {
        kafkaManager.destroy();
    }

    @Override
    public void addTopicIfNeeded(MQTopic topic) {
        kafkaManager.addTopicIfNeeded(topic);
    }

    @Override
    public void subscribe(String topic, MQMessageReceiver receiver) {
        kafkaManager.subscribe(topic, receiver);
    }

    @Override
    public void publish(String topic, Object message) {
        publish(topic, null, message);
    }

    @Override
    public void publish(String topic, String key, Object message) {
        if (message instanceof String) {
            kafkaManager.publish(topic, key, ((String) message).getBytes(StandardCharsets.UTF_8));
        } else if (message instanceof byte[]) {
            kafkaManager.publish(topic, key, message);
        } else {
            LOGGER.warn("publish failure with a unsupported wire type, topic: {}, message: {}", topic, message);
        }
    }

    @Override
    public void commitMessage(MQMessage message) {
        if (message instanceof KafkaMqMessage) {
            KafkaMqMessage kafkaMqMessage = (KafkaMqMessage)message;
            kafkaManager.commitOffset(new TopicPartition(kafkaMqMessage.getTopic(), kafkaMqMessage.getPartition()),
                    new OffsetAndMetadata(kafkaMqMessage.getOffset()));
        } else {
            LOGGER.error("commit unsupported wire type: {}", message);
        }
    }

    @Override
    protected String getName() {
        return "kafka mq client";
    }
}
