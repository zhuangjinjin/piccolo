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
package io.github.ukuz.piccolo.mq.rocketmq;

import io.github.ukuz.piccolo.api.PiccoloContext;
import io.github.ukuz.piccolo.api.mq.MQClient;
import io.github.ukuz.piccolo.api.mq.MQMessage;
import io.github.ukuz.piccolo.api.mq.MQMessageReceiver;
import io.github.ukuz.piccolo.api.mq.MQTopic;
import io.github.ukuz.piccolo.api.service.AbstractService;
import io.github.ukuz.piccolo.api.service.ServiceException;
import io.github.ukuz.piccolo.mq.rocketmq.manager.RocketMQManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

/**
 * @author ukuz90
 */
public class RocketMQClient extends AbstractService implements MQClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(RocketMQClient.class);
    private RocketMQManager rocketMQManager;

    @Override
    public void init(PiccoloContext context) throws ServiceException {
        rocketMQManager = new RocketMQManager(context);
        rocketMQManager.init();
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
        rocketMQManager.destroy();
    }

    @Override
    public void addTopicIfNeeded(MQTopic topic) {
        rocketMQManager.addTopicIfNeeded(topic);
    }

    @Override
    public void subscribe(String topic, MQMessageReceiver receiver) {
        rocketMQManager.subscribe(topic, receiver);
    }

    @Override
    public void publish(String topic, Object message) {
        publish(topic, null, message);
    }

    @Override
    public void publish(String topic, String key, Object message) {
        if (message instanceof String) {
            rocketMQManager.publish(topic, key, ((String) message).getBytes(StandardCharsets.UTF_8));
        } else if (message instanceof byte[]) {
            rocketMQManager.publish(topic, key, message);
        } else {
            LOGGER.warn("publish failure with a unsupported wire type, topic: {}, message: {}", topic, message);
        }
    }

    @Override
    public void commitMessage(MQMessage message) {
        if (message instanceof RocketMqMessage) {
            RocketMqMessage rocketMqMessage = (RocketMqMessage)message;
            rocketMQManager.commitOffset(rocketMqMessage);
        } else {
            LOGGER.error("commit unsupported wire type: {}", message);
        }
    }

    @Override
    protected String getName() {
        return "rocketmq client";
    }
}
