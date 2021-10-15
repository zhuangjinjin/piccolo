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
package io.github.ukuz.piccolo.mq.rocketmq.consumer;

import io.github.ukuz.piccolo.api.AsyncContext;
import io.github.ukuz.piccolo.api.common.utils.StringUtils;
import io.github.ukuz.piccolo.api.external.common.Assert;
import io.github.ukuz.piccolo.api.mq.MQMessageReceiver;
import io.github.ukuz.piccolo.mq.rocketmq.properties.RocketMQProperties;
import org.apache.rocketmq.acl.common.AclClientRPCHook;
import org.apache.rocketmq.acl.common.SessionCredentials;
import org.apache.rocketmq.client.AccessChannel;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.consumer.rebalance.AllocateMessageQueueAveragely;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.MessageExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * @author ukuz90
 */
public class RocketMQConsumerWorker implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(RocketMQConsumerWorker.class);
    private List<String> topics;
    private final MQMessageReceiver<byte[]> receiver;
    private final DefaultMQPushConsumer consumer;

    public RocketMQConsumerWorker(RocketMQProperties.ConsumerNestedProperties properties, List<String> topics, MQMessageReceiver receiver) {
        this.topics = topics;
        this.receiver = receiver;
        String accessKey = properties.getAccessKey();
        String secretKey = properties.getSecretKey();
        String accessChannel = properties.getAccessChannel();
        String namesrvAddr = properties.getNamesrvAddr();
        Assert.notEmptyString(namesrvAddr, "namesrvAddr must not empty");
        if (StringUtils.hasText(accessKey) && StringUtils.hasText(secretKey)) {
            this.consumer = new DefaultMQPushConsumer(properties.getGroupName(),
                new AclClientRPCHook(new SessionCredentials(accessKey, secretKey)), new AllocateMessageQueueAveragely(),
                true, null);
        } else {
            this.consumer = new DefaultMQPushConsumer(properties.getGroupName());
        }
        if (StringUtils.hasText(accessChannel)) {
            this.consumer.setAccessChannel(AccessChannel.valueOf(accessChannel));
        }
        this.consumer.setNamesrvAddr(namesrvAddr);
        // thread pool setting
        Optional.ofNullable(properties.getThreadPoolCoreSize()).ifPresent(consumer::setConsumeThreadMin);
        Optional.ofNullable(properties.getThreadPoolMaxSize()).ifPresent(consumer::setConsumeThreadMax);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void run() {
        try {
            topics.forEach(topic -> {
                try {
                    consumer.subscribe(topic, "*");
                } catch (MQClientException e) {
                    e.printStackTrace();
                }
            });
            this.consumer.registerMessageListener(
                (MessageListenerConcurrently)(msgs, consumeConcurrentlyContext) -> {
                    ConsumeConcurrentlyStatus status = null;
                    for (MessageExt msg : msgs) {
                        LOGGER.info("rocket mq received msg: {}", msg);
                        status = processMessageExt(msg);
                        if (status == ConsumeConcurrentlyStatus.RECONSUME_LATER) {
                            return status;
                        }
                    }
                    return status;
                });

            this.consumer.start();
        } catch (Exception e) {
            LOGGER.error("consume failure, err: {}", e.getCause());
        } finally {
            consumer.shutdown();
        }
    }

    private ConsumeConcurrentlyStatus processMessageExt(MessageExt messageExt) {
        try {
            CompletableFuture<Boolean> future = new CompletableFuture<>();
            AsyncContext.getContext().setArgument("future", future);
            // sync invoke
            receiver.receive(messageExt.getTopic(), messageExt.getBody());
            future.wait(10000);
            return future.isDone() ? ConsumeConcurrentlyStatus.CONSUME_SUCCESS :
                ConsumeConcurrentlyStatus.RECONSUME_LATER;
        } catch (Exception e) {
            LOGGER.error("consumer error log:{}",e);
            return ConsumeConcurrentlyStatus.RECONSUME_LATER;
        }
    }

    public void destroy() {
        this.consumer.shutdown();
    }
}
