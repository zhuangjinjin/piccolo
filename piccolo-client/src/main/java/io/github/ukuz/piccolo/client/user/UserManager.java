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
package io.github.ukuz.piccolo.client.user;

import io.github.ukuz.piccolo.api.PiccoloContext;
import io.github.ukuz.piccolo.api.common.Holder;
import io.github.ukuz.piccolo.api.external.common.Assert;
import io.github.ukuz.piccolo.api.mq.MQMessageReceiver;
import io.github.ukuz.piccolo.api.service.discovery.DefaultServiceInstance;
import io.github.ukuz.piccolo.client.PiccoloClient;
import io.github.ukuz.piccolo.common.ServiceNames;
import io.github.ukuz.piccolo.common.cache.CacheKeys;
import io.github.ukuz.piccolo.common.message.push.KafkaOfflineMqMessage;
import io.github.ukuz.piccolo.common.message.push.KafkaOnlineMqMessage;
import io.github.ukuz.piccolo.common.message.push.OfflineMqMessage;
import io.github.ukuz.piccolo.common.message.push.OnlineMqMessage;
import io.github.ukuz.piccolo.mq.kafka.Topics;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author ukuz90
 */
public class UserManager {

    private PiccoloContext piccoloContext;
    private Holder<Consumer<OfflineMqMessage>> offlineEventHandler = new Holder<>();

    public UserManager() {
        this.piccoloContext = PiccoloClient.getInstance();
    }

    /**
     * 总在线人数
     * @return
     */
    public long getAllOnlineUserNum() {
        List<String> serverIps = getAllServerIp();
        long onlineUserNum = 0;
        for (String serverIp : serverIps) {
            onlineUserNum += getOnlineUserNum(serverIp);
        }
        return onlineUserNum;
    }

    /**
     * 总在线人数清单
     * @return
     */
    public List<String> getAllOnlineUserList() {
        List<String> serverIps = getAllServerIp();
        List<String> allOnlineUserList = new ArrayList<>();
        for (String serverIp : serverIps) {
            String onlineUserListKey = CacheKeys.getOnlineUserListKey(serverIp);
            allOnlineUserList.addAll(piccoloContext.getCacheManager().zrange(onlineUserListKey, 0, -1, String.class));
        }
        return allOnlineUserList;
    }

    public List<String> getOnlineUserList(long start, long end) {
        Assert.isTrue(end >= start, "end must great than start, start: " + start + ", end: " + end);
        List<String> serverIps = getAllServerIp();
        List<String> onlineUserList = new ArrayList<>();
        long offset = 0;
        for (int i = 0; i < serverIps.size(); i++) {
            String ip = serverIps.get(i);
            String onlineUserListKey = CacheKeys.getOnlineUserListKey(ip);
            long num = getOnlineUserNum(ip);
            if (end > offset && start <= offset
                    || end > offset + num && start <= offset + num) {
                onlineUserList.addAll(piccoloContext.getCacheManager().zrange(onlineUserListKey, (int)(start - offset), (int)(end - offset), String.class));
            }
            if (end <= offset+num) {
                break;
            }
            offset += num;
        }
        return onlineUserList;
    }

    private long getOnlineUserNum(String ip) {
        String onlineUserListKey = CacheKeys.getOnlineUserListKey(ip);
        Long val = piccoloContext.getCacheManager().zCard(onlineUserListKey);
        return val == null ? 0 : val.longValue();
    }

    private List<String> getAllServerIp() {
        List<DefaultServiceInstance> list = piccoloContext.getServiceDiscovery().lookup(ServiceNames.S_GATEWAY);
        List<String> serverIps = new ArrayList<>(list.size());
        list.forEach(instance -> serverIps.add(instance.getHost()));
        return serverIps;
    }

    /**
     * 注册下线回调
     * @param offlineHandler
     */
    public void registerOfflineHandler(Consumer<OfflineMqMessage> offlineHandler) {
        Assert.notNull(offlineHandler, "offlineHandler must not be null");
        this.piccoloContext.getMQClient().subscribe(Topics.OFFLINE_MESSAGE.getTopic(), new OfflineMessageReceiver(offlineHandler));
    }

    /**
     * 注册上线回调
     * @param onlineHandler
     */
    public void registerOnlineHandler(Consumer<OnlineMqMessage> onlineHandler) {
        Assert.notNull(onlineHandler, "onlineHandler must not be null");
        this.piccoloContext.getMQClient().subscribe(Topics.ONLINE_MESSAGE.getTopic(), new OnlineMessageReceiver(onlineHandler));
    }

    private class OfflineMessageReceiver implements MQMessageReceiver<byte[]> {

        private final Consumer<OfflineMqMessage> consumer;

        public OfflineMessageReceiver(Consumer<OfflineMqMessage> consumer) {
            this.consumer = consumer;
        }

        @Override
        public void receive(String topic, byte[] message, Object... attachment) {
            Assert.isTrue(attachment[0] instanceof TopicPartition, "attachment[0] must be TopicPartition");
            Assert.isTrue(attachment[1] instanceof OffsetAndMetadata, "attachment[1] must be OffsetAndMetadata");

            String uid = new String(message, StandardCharsets.UTF_8);

            KafkaOfflineMqMessage msg = new KafkaOfflineMqMessage();
            TopicPartition topicPartition = (TopicPartition) attachment[0];
            OffsetAndMetadata offsetAndMetadata = (OffsetAndMetadata) attachment[1];
            msg.setUid(uid);
            msg.setMqClient(UserManager.this.piccoloContext.getMQClient());
            msg.setTopic(topicPartition.topic());
            msg.setPartition(topicPartition.partition());
            msg.setOffset(offsetAndMetadata.offset());
            this.consumer.accept(msg);
        }
    }

    private class OnlineMessageReceiver implements MQMessageReceiver<byte[]> {

        private final Consumer<OnlineMqMessage> consumer;

        public OnlineMessageReceiver(Consumer<OnlineMqMessage> consumer) {
            this.consumer = consumer;
        }

        @Override
        public void receive(String topic, byte[] message, Object... attachment) {
            Assert.isTrue(attachment[0] instanceof TopicPartition, "attachment[0] must be TopicPartition");
            Assert.isTrue(attachment[1] instanceof OffsetAndMetadata, "attachment[1] must be OffsetAndMetadata");

            String uid = new String(message, StandardCharsets.UTF_8);

            KafkaOnlineMqMessage msg = new KafkaOnlineMqMessage();
            TopicPartition topicPartition = (TopicPartition) attachment[0];
            OffsetAndMetadata offsetAndMetadata = (OffsetAndMetadata) attachment[1];
            msg.setUid(uid);
            msg.setMqClient(UserManager.this.piccoloContext.getMQClient());
            msg.setTopic(topicPartition.topic());
            msg.setPartition(topicPartition.partition());
            msg.setOffset(offsetAndMetadata.offset());
            this.consumer.accept(msg);
        }
    }

}
