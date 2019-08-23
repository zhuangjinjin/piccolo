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

import io.github.ukuz.piccolo.api.mq.MQTopic;
import io.github.ukuz.piccolo.mq.properties.MQTopicProperties;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.utils.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

/**
 * @author ukuz90
 */
public class KafkaAdmin {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaAdmin.class);
    private final Map<String, Object> config;
    private long operationTimeout = 30;
    private long closeTimeout = 10;
    private MQTopicProperties topicProperties;

    public KafkaAdmin( MQTopicProperties topicProperties, Map<String, Object> config) {
        this.config = config;
        this.topicProperties = topicProperties;
    }

    public void init() {
        List<NewTopic> newTopics = findAllTopics();
        checkAndAddTopic(newTopics);
    }

    private void checkAndAddTopic(List<NewTopic> newTopics) {
        if (newTopics != null && !newTopics.isEmpty()) {
            AdminClient adminClient = null;
            try {
                adminClient = AdminClient.create(config);
            } catch (Exception e) {
                LOGGER.error("Could not create adminClient, cause: {}", e);
                throw new IllegalStateException("Could not create adminClient", e);
            }
            try {
                if (adminClient != null) {
                    addTopicsIfNeeded(adminClient, newTopics);
                }
            } catch (Exception e) {
                LOGGER.error("Init topics failure, cause: {}", e);
            } finally {
                if (adminClient != null) {
                    adminClient.close(closeTimeout, TimeUnit.SECONDS);
                }
            }
        }
    }

    private void addTopicsIfNeeded(AdminClient adminClient, List<NewTopic> newTopics) {
        if (newTopics.size() > 0) {
            Map<String, NewTopic> topicNameToTopic = new HashMap<>();
            newTopics.forEach(topic -> topicNameToTopic.compute(topic.name(), (k, v) -> topic));
            List<String> topicNames = newTopics.stream()
                    .map(NewTopic::name)
                    .collect(Collectors.toList());
            DescribeTopicsResult topicInfo = adminClient.describeTopics(topicNames);
            List<NewTopic> topicToAdd = new ArrayList<>();
            Map<String, NewPartitions> topicToModify = checkPartitions(topicNameToTopic, topicInfo, topicToAdd);

            if (topicToAdd.size() > 0) {
                LOGGER.info("Found some topics need creation to broker, topics: {} size: {}", topicToAdd, topicToAdd.size());
                addTopics(adminClient, topicToAdd);
            }
            if (topicToModify.size() > 0) {
                LOGGER.info("Found some topics need creation partition count, topics: {} size: {}", topicToModify, topicToModify.size());
                modifyTopics(adminClient, topicToModify);
            }

        }
    }

    private Map<String, NewPartitions> checkPartitions(Map<String, NewTopic> topicNameToTopic,
                                                  DescribeTopicsResult topicInfo, List<NewTopic> topicsToAdd) {
        Map<String, NewPartitions> topicsToModify = new HashMap<>();
        topicInfo.values().forEach((name, future) -> {
            NewTopic topic = topicNameToTopic.get(name);
            if (topic != null) {
                try {
                    TopicDescription td = future.get(this.operationTimeout, TimeUnit.SECONDS);
                    if (topic.numPartitions() < td.partitions().size()) {
                        LOGGER.info("Topic {} exist but has different partition count, now {} not {}",
                                name, td.partitions().size(), topic.numPartitions());
                    } else if (topic.numPartitions() > td.partitions().size()){
                        LOGGER.info("Topic {} exist but has different partition count, now {} not {}, increasing if broker support it",
                                name, td.partitions().size(), topic.numPartitions());

                        topicsToModify.put(name, NewPartitions.increaseTo(topic.numPartitions()));
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException e) {
                    topicsToAdd.add(topic);
                } catch (TimeoutException e) {
                    throw new KafkaException("Time out waiting for get exist topic");
                }
            }
        });
        return topicsToModify;
    }

    private void modifyTopics(AdminClient adminClient, Map<String, NewPartitions> topicsToModify) {
        if (topicsToModify.size() > 0) {
            CreatePartitionsResult partitionsResult = adminClient.createPartitions(topicsToModify);
            try {
                partitionsResult.all().get(this.operationTimeout, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOGGER.error("Interrupted while waiting for partition creation results, cause: {}", e);
            } catch (ExecutionException e) {
                LOGGER.error("Failed to create partitions, cause: {}", e);
                if (!(e.getCause() instanceof UnsupportedClassVersionError)) {
                    throw new KafkaException("Failed to create partitions", e);
                }
            } catch (TimeoutException e) {
                throw new KafkaException("Timed out waiting for create partitions results", e);
            }
        }
    }

    private void addTopics(AdminClient adminClient, List<NewTopic> topicToAdd) {
        if (topicToAdd.size() > 0) {
            LOGGER.info("addTopics topics: {}", topicToAdd);
            CreateTopicsResult topicsResult = adminClient.createTopics(topicToAdd);
            try {
                topicsResult.all().get(this.operationTimeout, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOGGER.error("Interrupted while waiting for topics creation results, cause: {}", e);
            } catch (ExecutionException e) {
                LOGGER.error("Failed to create topics, cause: {}", e);
                if (!(e.getCause() instanceof UnsupportedClassVersionError)) {
                    throw new KafkaException("Failed to create topics", e);
                }
            } catch (TimeoutException e) {
                throw new KafkaException("Timed out waiting for create topics results", e);
            }
        }
    }

    private List<NewTopic> findAllTopics() {
        List<MQTopic> list = topicProperties.getAllTopics();
        if (list.isEmpty()) {
            return Collections.emptyList();
        }
        List<NewTopic> topics = list.stream()
                .map(t -> new NewTopic(t.getTopic(), t.getNumPartitions(), t.getReplicationFactor()))
                .collect(Collectors.toList());
        return topics;
    }

    public void addTopicIfNeeded(MQTopic topic) {
        NewTopic newTopic = new NewTopic(topic.getTopic(), topic.getNumPartitions(), topic.getReplicationFactor());
        checkAndAddTopic(Collections.singletonList(newTopic));
    }


}
