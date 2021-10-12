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
package io.github.ukuz.piccolo.common.message.push;

import io.github.ukuz.piccolo.mq.CommittableMqMessage;

/**
 * @author ukuz90
 */
public class BaseDispatcherMqMessage extends CommittableMqMessage implements DispatcherMqMessage {

    private byte[] payload;
    private String uid;

    @Override
    public byte[] getPayload() {
        return payload;
    }

    @Override
    public String getUid() {
        return uid;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    @Override
    protected void doEncode0() {
        writeString(uid);
        writeBytes(payload);
    }

    @Override
    protected void doDecode0() {
        uid = readString();
        payload = readBytes();
    }

    @Override
    public void completeConsume() {
        mqClient.commitMessage(this);
    }
}
