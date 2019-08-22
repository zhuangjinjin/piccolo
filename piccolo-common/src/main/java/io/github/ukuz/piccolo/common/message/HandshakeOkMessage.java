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
package io.github.ukuz.piccolo.common.message;

import io.github.ukuz.piccolo.api.connection.Connection;
import io.github.ukuz.piccolo.api.exchange.support.ByteBufMessage;
import static io.github.ukuz.piccolo.common.constants.CommandType.*;
import io.netty.buffer.ByteBuf;
import lombok.ToString;

/**
 * @author ukuz90
 */
@ToString
public class HandshakeOkMessage extends ByteBufMessage {

    public int heartbeat;
    public long expireTime;
    public byte[] serverKey;
    public String sessionId;

    public HandshakeOkMessage(Connection connection) {
        super(connection, HANDSHAKE.getCmd());
    }

    @Override
    protected void decodeBody0(ByteBuf buf) {
        heartbeat = readInt(buf);
        expireTime = readLong(buf);
        serverKey = readBytes(buf);
        sessionId = readString(buf);
    }

    @Override
    protected void encodeBody0(ByteBuf buf) {
        writeInt(buf, heartbeat);
        writeLong(buf, expireTime);
        writeBytes(buf, serverKey);
        writeString(buf, sessionId);
    }

    public static HandshakeOkMessage build(Connection connection) {
        return new HandshakeOkMessage(connection);
    }

    public HandshakeOkMessage heartbeat(int heartbeat) {
        this.heartbeat = heartbeat;
        return this;
    }

    public HandshakeOkMessage expireTime(long expireTime) {
        this.expireTime = expireTime;
        return this;
    }

    public HandshakeOkMessage serverKey(byte[] serverKey) {
        this.serverKey = serverKey;
        return this;
    }

    public HandshakeOkMessage sessionId(String sessionId) {
        this.sessionId = sessionId;
        return this;
    }

}
