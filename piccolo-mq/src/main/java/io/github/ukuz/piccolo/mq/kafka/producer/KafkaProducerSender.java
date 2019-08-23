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
package io.github.ukuz.piccolo.mq.kafka.producer;

import io.github.ukuz.piccolo.api.service.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.Map;
import java.util.Optional;

/**
 * @author ukuz90
 */
public class KafkaProducerSender {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaProducerSender.class);

    private final Map<String, Object> producerProps;
    private KafkaProducer producer;

    public KafkaProducerSender(Map<String, Object> producerProps) {
        this.producerProps = producerProps;
    }

    public void init() {
        producer = new KafkaProducer(producerProps);
    }

    @SuppressWarnings("unchecked")
    public void send(String topic, byte[] content, Callback callback) {
        ProducerRecord<String, byte[]> record = new ProducerRecord<>(topic, content);
        producer.send(record, ((metadata, exception) -> {
            if (exception == null) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("producer send success, message: {}", content);
                }
                Optional.ofNullable(callback).ifPresent(Callback::success);
            } else {
                //TODO need throw exception ?
                LOGGER.error("producer send failure, message: {}", exception);
                Optional.ofNullable(callback).ifPresent(cb -> cb.failure(exception));
            }
        }));
    }

    public void destroy() {
        if (producer != null) {
            producer.close();
        }
    }
}
