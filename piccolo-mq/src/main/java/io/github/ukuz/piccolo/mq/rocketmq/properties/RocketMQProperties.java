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
package io.github.ukuz.piccolo.mq.rocketmq.properties;

import io.github.ukuz.piccolo.api.config.ConfigurationProperties;
import io.github.ukuz.piccolo.api.config.Properties;
import lombok.Data;

/**
 * @author ukuz90
 */
@ConfigurationProperties(prefix = "piccolo.rocketmq")
@Data
public class RocketMQProperties implements Properties {

    private ProducerNestedProperties producer;
    private ConsumerNestedProperties consumer;
    private AdminClientNestedProperties adminClient;

    @Data
    public class ConsumerNestedProperties extends ClientConfigProperties {
        private Integer threadPoolCoreSize;
        private Integer threadPoolMaxSize;
    }

    @Data
    public class ProducerNestedProperties extends ClientConfigProperties {
        private Integer threadPoolCoreSize;
        private Integer threadPoolMaxSize;
    }

    @Data
    public class ClientConfigProperties implements Properties {
        private String groupName;
        private String namesrvAddr;
        private String accessChannel;
        private String accessKey;
        private String secretKey;
        private String namespace;
        private Integer pullNameServerInterval;
        private Integer heartbeatBrokerInterval;
        private Integer persistConsumerOffsetInterval;
    }

    @Data
    public class AdminClientNestedProperties implements Properties {
        private String bootstrapServers;
    }

}
