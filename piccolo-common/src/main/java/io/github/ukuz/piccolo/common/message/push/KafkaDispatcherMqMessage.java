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
package io.github.ukuz.piccolo.common.message.push;

import io.github.ukuz.piccolo.mq.kafka.KafkaMqMessage;

/**
 * @author ukuz90
 */
public class KafkaDispatcherMqMessage extends KafkaMqMessage implements DispatcherMqMessage {

    private byte[] payload;

    @Override
    public byte[] getPayload() {
        return payload;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }

    @Override
    protected void doEncode0() {
        writeBytes(payload);
    }

    @Override
    protected void doDecode0() {
        payload = readBytes();
    }

}
