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
package io.github.ukuz.piccolo.common.router;

import io.github.ukuz.piccolo.mq.kafka.KafkaMqMessage;

/**
 * @author ukuz90
 */
public class KafkaKickMqMessage extends KafkaMqMessage implements KickMqMessage {

    private String userId;
    private String deviceId;
    private String connId;
    private byte clientType;
    private String targetAddress;
    private int targetPort;

    @Override
    protected void doEncode0() {
        writeString(userId);
        writeString(deviceId);
        writeString(connId);
        writeByte(clientType);
        writeString(targetAddress);
        writeInt(targetPort);
    }

    @Override
    protected void doDecode0() {
        userId = readString();
        deviceId = readString();
        connId = readString();
        clientType = readByte();
        targetAddress = readString();
        targetPort = readInt();
    }

    @Override
    public long getXid() {
        return xid;
    }

    @Override
    public String getUserId() {
        return userId;
    }

    @Override
    public String getDeviceId() {
        return deviceId;
    }

    @Override
    public String getConnId() {
        return connId;
    }

    @Override
    public byte getClientType() {
        return clientType;
    }

    @Override
    public String getTargetAddress() {
        return targetAddress;
    }

    @Override
    public int getTargetPort() {
        return targetPort;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public void setConnId(String connId) {
        this.connId = connId;
    }

    public void setClientType(byte clientType) {
        this.clientType = clientType;
    }

    public void setTargetAddress(String targetAddress) {
        this.targetAddress = targetAddress;
    }

    public void setTargetPort(int targetPort) {
        this.targetPort = targetPort;
    }
}
