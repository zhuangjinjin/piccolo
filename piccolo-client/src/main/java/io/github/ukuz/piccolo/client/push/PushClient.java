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
package io.github.ukuz.piccolo.client.push;

import io.github.ukuz.piccolo.api.AsyncContext;
import io.github.ukuz.piccolo.api.connection.Connection;
import io.github.ukuz.piccolo.api.external.common.Assert;
import io.github.ukuz.piccolo.api.id.IdGenException;
import io.github.ukuz.piccolo.api.mq.MQMessageReceiver;
import io.github.ukuz.piccolo.api.push.PushContext;
import io.github.ukuz.piccolo.api.service.discovery.ServiceInstance;
import io.github.ukuz.piccolo.client.PiccoloClient;
import io.github.ukuz.piccolo.client.id.IdGenBuilder;
import io.github.ukuz.piccolo.common.ServiceNames;
import io.github.ukuz.piccolo.common.message.PushMessage;
import io.github.ukuz.piccolo.common.message.push.KafkaDispatcherMqMessage;
import io.github.ukuz.piccolo.common.message.push.RocketMQDispatcherMqMessage;
import io.github.ukuz.piccolo.common.properties.CoreProperties;
import io.github.ukuz.piccolo.common.router.RemoteRouter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;

import static io.github.ukuz.piccolo.api.common.threadpool.ExecutorFactory.PUSH_CLIENT;
import static io.github.ukuz.piccolo.mq.kafka.Topics.DISPATCH_MESSAGE;

/**
 * @author ukuz90
 */
public class PushClient implements AutoCloseable {

    private ConcurrentMap<String, NestedMessageReceiver> topicsHandler = new ConcurrentHashMap<>();
    private PiccoloClient piccoloClient;
    private static final Logger LOGGER = LoggerFactory.getLogger(PushClient.class);
    private ExecutorService dispatchHandlerExecutor;

    public PushClient() {
        piccoloClient = PiccoloClient.getInstance();
        dispatchHandlerExecutor = (ExecutorService) piccoloClient.getExecutorFactory().create(PUSH_CLIENT, piccoloClient.getEnvironment());
    }

    /**
     * register a logical handler
     * @param handler
     */
    public void registerHandler(BaseDispatcherHandler handler) {
        Assert.notNull(handler, "handler must not be null");
        registerHandler(DISPATCH_MESSAGE.getTopic(), handler);
    }

    /**
     * push message to gateway server
     * @param context
     */
    public void push(PushContext context) {
        Assert.notNull(context, "context must not be null");
        //先查询client所在的网关服务器的地址
        if (context.getUserId() != null) {
            pushSingleUser(context.getUserId(), context.getContext());
        } else if (context.getUserIds() != null && !CollectionUtils.isEmpty(context.getUserIds())) {
            context.getUserIds().forEach(userId -> pushSingleUser(userId, context.getContext()));
        } else if (context.isBroadcast()) {
            broadcast(context.getContext());
        }
    }

    /**
     * generate an xid in distributed system
     * this method is deprecated, we support to use {@link IdGenBuilder#build()}
     * @return
     * @throws IdGenException
     */
    @Deprecated
    public long getXid() throws IdGenException {
        return piccoloClient.getIdGen().get(null);
    }

    private void pushSingleUser(String userId, byte[] context) {
        Set<RemoteRouter> remoteRouters = piccoloClient.getRemoteRouterManager().lookupAll(userId);
        remoteRouters
                .stream()
                .filter(RemoteRouter::isOnline)
                .forEach(remoteRouter -> {
            Connection connection = piccoloClient.getGatewayConnectionFactory().getConnection(remoteRouter.getRouterValue().getHostAndPort());
            if (connection != null) {
                PushMessage msg = PushMessage.build(connection).content(context).userId(userId);
                connection.sendAsync(msg);
            } else {
                LOGGER.error("can not push message to gateway server, is it work, userId: {} server: {}",
                        userId,
                        remoteRouter.getRouterValue().getHostAndPort());
                //TODO 是否重试？
            }
        });
    }

    private void broadcast(byte[] context) {
        List<ServiceInstance> serviceInstances = piccoloClient.getServiceDiscovery().lookup(ServiceNames.S_GATEWAY);
        serviceInstances.forEach(serviceInstance -> {
            Connection connection = piccoloClient.getGatewayConnectionFactory().getConnection(serviceInstance.getHostAndPort());
            if (connection != null) {
                PushMessage msg = PushMessage.build(connection).content(context).broadcast(true);
                connection.sendAsync(msg);
            } else {
                LOGGER.error("can not push message to gateway server, is it work, server: {}",
                        serviceInstance.getHostAndPort());
                //TODO 是否重试？
            }
        });
    }

    public void registerHandler(String topic, BaseDispatcherHandler handler) {
        Assert.notNull(topic, "topic must not be null");
        Assert.notNull(handler, "handler must not be null");
        LOGGER.info("registerHandler topic: {} handler: {}", topic, handler);

        topicsHandler.computeIfAbsent(topic, t -> new NestedMessageReceiver(handler));
        piccoloClient.getMQClient().subscribe(topic, topicsHandler.get(topic));
    }

    @Override
    public void close() throws Exception {
        dispatchHandlerExecutor.shutdown();
        piccoloClient.destroy();
    }

    private class NestedMessageReceiver implements MQMessageReceiver<byte[]> {

        private BaseDispatcherHandler handler;

        public NestedMessageReceiver(BaseDispatcherHandler handler) {
            Assert.notNull(handler, "handler must not be null");
            this.handler = handler;
        }

        @Override
        public void receive(String topic, byte[] message, Object... attachment) {
            CoreProperties core = piccoloClient.getProperties(CoreProperties.class);
            if (Objects.equals(core.getMq(), "kafka")) {
                receiveFromKafka(message, attachment[0], attachment[1]);
            } else {
                receiveFromRocketMQ(message);
            }
        }

        private void receiveFromKafka(byte[] message, Object... attachment) {
            Assert.isTrue(attachment[0] instanceof TopicPartition, "attachment[0] must be TopicPartition");
            Assert.isTrue(attachment[1] instanceof OffsetAndMetadata, "attachment[1] must be OffsetAndMetadata");
            TopicPartition topicPartition = (TopicPartition) attachment[0];
            OffsetAndMetadata offsetAndMetadata = (OffsetAndMetadata) attachment[1];
            KafkaDispatcherMqMessage msg = new KafkaDispatcherMqMessage();
            msg.setMqClient(piccoloClient.getMQClient());
            msg.setTopic(topicPartition.topic());
            msg.setPartition(topicPartition.partition());
            msg.setOffset(offsetAndMetadata.offset());
            msg.decode(message);
            dispatchHandlerExecutor.execute(() -> handler.onDispatch(msg));
        }

        @SuppressWarnings("uncheck")
        private void receiveFromRocketMQ(byte[] message) {
            CompletableFuture<Boolean> future =
                (CompletableFuture<Boolean>)AsyncContext.getContext().getArgument("future");
            RocketMQDispatcherMqMessage msg = new RocketMQDispatcherMqMessage();
            msg.setMqClient(piccoloClient.getMQClient());
            msg.setFuture(future);
            msg.decode(message);
            dispatchHandlerExecutor.execute(() -> handler.onDispatch(msg));
        }
    }

}
