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
package io.github.ukuz.piccolo.mq.rocketmq.producer;

import io.github.ukuz.piccolo.api.common.utils.StringUtils;
import io.github.ukuz.piccolo.api.external.common.Assert;
import io.github.ukuz.piccolo.api.service.Callback;
import io.github.ukuz.piccolo.mq.rocketmq.properties.RocketMQProperties;
import lombok.SneakyThrows;
import org.apache.rocketmq.acl.common.AclClientRPCHook;
import org.apache.rocketmq.acl.common.SessionCredentials;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.TransactionMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * @author ukuz90
 */
public class RocketMQProducerSender {

    private static final Logger LOGGER = LoggerFactory.getLogger(RocketMQProducerSender.class);

    private DefaultMQProducer producer;

    public RocketMQProducerSender(RocketMQProperties.ProducerNestedProperties properties) {
        String accessKey = properties.getAccessKey();
        String secretKey = properties.getSecretKey();
        String namesrvAddr = properties.getNamesrvAddr();
        Assert.notEmptyString(namesrvAddr, "namesrvAddr must not empty");
        if (StringUtils.hasText(accessKey) && StringUtils.hasText(secretKey)) {
            this.producer = new TransactionMQProducer(properties.getGroupName(),
                new AclClientRPCHook(new SessionCredentials(accessKey, secretKey)));
        } else {
            this.producer = new TransactionMQProducer(properties.getGroupName());
        }
        this.producer.setNamesrvAddr(namesrvAddr);
    }

    public void init() {
    }

    @SneakyThrows
    @SuppressWarnings("unchecked")
    public void send(String topic, String key, byte[] content, Callback callback) {
        Message message = new Message(topic, key, content);
        producer.send(message, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("producer send success, message: {}", content);
                }
                Optional.ofNullable(callback).ifPresent(Callback::success);
            }

            @Override
            public void onException(Throwable throwable) {
                LOGGER.error("producer send failure, message: {}", throwable);
                Optional.ofNullable(callback).ifPresent(cb -> cb.failure(throwable));
            }
        });
    }

    public void destroy() {
        if (producer != null) {
            producer.shutdown();
        }
    }
}
