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
package io.github.ukuz.piccolo.mq.kafka.properties;

import io.github.ukuz.piccolo.api.config.ConfigurationProperties;
import io.github.ukuz.piccolo.api.config.Properties;
import io.github.ukuz.piccolo.api.external.properties.PropertyMapper;
import lombok.Data;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.*;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author ukuz90
 */
@ConfigurationProperties(prefix = "piccolo.kafka")
@Data
public class KafkaProperties implements Properties {

    private ProducerNestedProperties producer;
    private ConsumerNestedProperties consumer;
    private AdminClientNestedProperties adminClient;

    @Data
    public class ProducerNestedProperties implements Properties {
        private String bootstrapServers;
        private Integer lingerMs;
        private Integer batchSize;
        /**
         * values are <code>none</code>, <code>gzip</code>, <code>snappy</code>, <code>lz4</code> or <code>zstd(kafka 2.1+)</code>
         * zstd > lz4 > snappy > gzip
         */
        private String compressType;
        private Integer maxInFlightRequestsPerConnection;
        private Integer acks;
        private Integer retries;
        private Integer retriesBackoffMs;
        private Integer requestTimeoutMs;
        private final HashMap<String, Object> properties = new HashMap<>();

        public Map<String, Object> buildProperties() {
            NestedProperties props = new NestedProperties();
            PropertyMapper map = PropertyMapper.get().alwaysApplyingWhenNonNull();
            //必填参数
            map.from(this::getBootstrapServers).to(props.in(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG));
            map.from(StringSerializer.class::getName).to(props.in(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG));
            map.from(ByteArraySerializer.class::getName).to(props.in(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG));

            /////////////////////调优参数/////////////////////
            //最重要的参数之一
            map.from(this::getLingerMs).to(props.in(ProducerConfig.LINGER_MS_CONFIG));
            map.from(this::getBatchSize).to(props.in(ProducerConfig.BATCH_SIZE_CONFIG));
            //压缩参数，降低网络I/O传输开销，增加CPU开销（默认none）
            map.from(this::getCompressType).to(props.in(ProducerConfig.COMPRESSION_TYPE_CONFIG));
            //设置一次只允许producer发送一个消息，防止消息重发时产生乱序 (默认5个)
            map.from(this::getMaxInFlightRequestsPerConnection).to(props.in(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION));
            //1代表producer发送后leader broker仅写入本地日志后，然后发送响应结果给producer，无须等ISR完成 (默认值是1)
            map.from(this::getAcks).to(props.in(ProducerConfig.ACKS_CONFIG));
            //设置重试次数（2.0.0默认0次，2.1.0开始默认是Integer.MAX_VALUE次）
            map.from(this::getRetries).to(props.in(ProducerConfig.RETRIES_CONFIG));
            //两次重试之间会有一段停顿时间（默认是100ms）
            map.from(this::getRetriesBackoffMs).to(props.in(ProducerConfig.RETRY_BACKOFF_MS_CONFIG));
            //发送到broker，broker的响应时间上限（默认30秒）
            map.from(this::getRequestTimeoutMs).to(props.in(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG));

            return props.with(properties);
        }
    }

    @Data
    public class ConsumerNestedProperties implements Properties {

        private String bootstrapServers;
        private String groupId;
        private String autoOffsetReset;
        private Integer sessionTimeout;
        private Integer maxPollIntervalMs;
        private Boolean enableAutoCommit;
        private Integer autoCommitIntervalMs;
        private Integer fetchMaxBytes;
        private Integer maxPollRecords;
        private Integer heartbeatIntervalMs;
        private Integer connectionsIntervalMs;

        private final HashMap<String, Object> properties = new HashMap<>();

        public Map<String, Object> buildProperties() {
            NestedProperties props = new NestedProperties();
            PropertyMapper map = PropertyMapper.get().alwaysApplyingWhenNonNull();
            //必填参数
            map.from(this::getBootstrapServers).to(props.in(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG));
            map.from(this::getGroupId).to(props.in(ConsumerConfig.GROUP_ID_CONFIG));
            map.from(StringDeserializer.class::getName).to(props.in(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG));
            map.from(ByteArrayDeserializer.class::getName).to(props.in(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG));
            //offset从最早的开始，默认是从最近的开始
            map.from(this::getAutoOffsetReset).to(props.in(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG));

            /////////////////////调优参数/////////////////////
            //coordinator感应consumer_group崩溃时间（默认10秒）
            map.from(this::getSessionTimeout).to(props.in(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG));
            //consumer处理逻辑的最大时间（默认5分钟）
            map.from(this::getMaxPollIntervalMs).to(props.in(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG));
            //自动确认，真实场景下需要手动确认（默认true）
            map.from(this::getEnableAutoCommit).to(props.in(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG));
            map.from(this::getAutoCommitIntervalMs).to(props.in(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG));
            //单次获取数据的最大字节数（默认50M）
            map.from(this::getFetchMaxBytes).to(props.in(ConsumerConfig.FETCH_MAX_BYTES_CONFIG));
            //每次最多能拉取的消息数（默认500）
            map.from(this::getMaxPollRecords).to(props.in(ConsumerConfig.MAX_POLL_RECORDS_CONFIG));
            //心跳间隔时间（默认3秒）
            map.from(this::getHeartbeatIntervalMs).to(props.in(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG));
            //kafka会定期关闭（默认9分钟）
            map.from(this::getConnectionsIntervalMs).to(props.in(ConsumerConfig.CONNECTIONS_MAX_IDLE_MS_CONFIG));
            return props.with(properties);
        }
    }

    @Data
    public class AdminClientNestedProperties implements Properties {
        private String bootstrapServers;

        private final HashMap<String, Object> properties = new HashMap<>();

        public Map<String, Object> buildProperties() {
            NestedProperties props = new NestedProperties();
            PropertyMapper map = PropertyMapper.get().alwaysApplyingWhenNonNull();
            map.from(this::getBootstrapServers).to(props.in(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG));
            return props.with(properties);
        }
    }

    public Map<String, Object> buildProducerProperties() {
        return producer.buildProperties();
    }

    public Map<String, Object> buildConsumerProperties() {
        return consumer.buildProperties();
    }

    public Map<String, Object> buildAdminClientProperties() {
        return adminClient.buildProperties();
    }

    public static class NestedProperties extends HashMap<String, Object> {

        public <V> Consumer<V> in(String key) {
            return value -> put(key, value);
        }

        public NestedProperties with(Map<String, Object> map) {
            putAll(map);
            return this;
        }

    }

}
