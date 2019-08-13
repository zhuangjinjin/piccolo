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
package io.github.ukuz.piccolo.mq.kafka.consumer;

import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.util.Map;

/**
 * @author ukuz90
 */
public class KafkaConsumerWorker implements Runnable {

    private final Map<String, Object> properties;

    public KafkaConsumerWorker(Map<String, Object> properties) {
        this.properties = properties;
    }

    @Override
    public void run() {
        try(KafkaConsumer consumer = new KafkaConsumer(properties)) {

        }
    }
}
