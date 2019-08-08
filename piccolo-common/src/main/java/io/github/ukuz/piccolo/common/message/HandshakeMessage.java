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
package  io.github.ukuz.piccolo.common.message;

import io.github.ukuz.piccolo.api.connection.Connection;
import io.github.ukuz.piccolo.api.exchange.support.ByteBufMessage;
import static io.github.ukuz.piccolo.common.constants.CommandType.*;
import io.netty.buffer.ByteBuf;

import java.util.Arrays;

/**
 * @author ukuz90
 */
public class HandshakeMessage extends ByteBufMessage {

    public String deviceId;
    public String osName;
    public String osVersion;
    public String clientVersion;
    public byte[] iv;
    public byte[] clientKey;
    public int minHeartbeat;
    public int maxHeartbeat;
    public long timestamp;

    public HandshakeMessage(Connection connection) {
        super(connection, HANDSHAKE.getCmd());
    }

    @Override
    protected void decodeBody0(ByteBuf buf) {
        this.deviceId = readString(buf);
        this.osName = readString(buf);
        this.osVersion = readString(buf);
        this.clientVersion = readString(buf);
        this.iv = readBytes(buf);
        this.clientKey = readBytes(buf);
        this.minHeartbeat = readInt(buf);
        this.maxHeartbeat = readInt(buf);
        this.timestamp = readLong(buf);
    }

    @Override
    protected void encodeBody0(ByteBuf buf) {
        this.writeString(buf, deviceId);
        this.writeString(buf, osName);
        this.writeString(buf, osVersion);
        this.writeString(buf, clientVersion);
        this.writeBytes(buf, iv);
        this.writeBytes(buf, clientKey);
        this.writeInt(buf, minHeartbeat);
        this.writeInt(buf, maxHeartbeat);
        this.writeLong(buf, timestamp);
    }

    @Override
    public String toString() {
        return "HandshakeMessage{" +
                "deviceId='" + deviceId + '\'' +
                ", osName='" + osName + '\'' +
                ", osVersion='" + osVersion + '\'' +
                ", clientVersion='" + clientVersion + '\'' +
                ", iv=" + Arrays.toString(iv) +
                ", clientKey=" + Arrays.toString(clientKey) +
                ", minHeartbeat=" + minHeartbeat +
                ", maxHeartbeat=" + maxHeartbeat +
                ", timestamp=" + timestamp +
                '}';
    }
}
