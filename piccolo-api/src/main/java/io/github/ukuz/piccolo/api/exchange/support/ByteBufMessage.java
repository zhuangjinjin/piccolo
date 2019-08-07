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
package io.github.ukuz.piccolo.api.exchange.support;

import io.github.ukuz.piccolo.api.exchange.protocol.Packet;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;

import java.nio.charset.StandardCharsets;

/**
 * @author ukuz90
 */
public abstract class ByteBufMessage implements BaseMessage {

    private byte commandType;
    private byte compressType;
    private int sessionId;
    private Channel channel;

    public ByteBufMessage(Channel channel, byte commandType) {
        this.channel = channel;
        this.commandType = commandType;
    }

    @Override
    public void decodeBody(Packet packet) {
        ByteBuf buf = Unpooled.wrappedBuffer(packet.getPayload());
        decodeBody0(buf);
    }

    @Override
    public Packet encodeBody() {
        Packet packet = new Packet();
        packet.setMagic((short) 0xbcc0);
        packet.setFlag(getFlag());
        packet.setSessionId(sessionId);

//        ByteBuf buf = PooledByteBufAllocator.DEFAULT.buffer();
        ByteBuf buf = channel.alloc().buffer();
        encodeBody0(buf);
        byte[] payload = new byte[buf.readableBytes()];
        buf.readBytes(payload);

        packet.setPayload(payload);
        packet.setLength(payload.length);
        return packet;
    }

    public void writeByte(ByteBuf buf, byte b) {
        buf.writeByte(b);
    }

    public void writeShort(ByteBuf buf, short s) {
        buf.writeShort(s);
    }

    public void writeInt(ByteBuf buf, int i) {
        buf.writeInt(i);
    }

    public void writeLong(ByteBuf buf, long l) {
        buf.writeLong(l);
    }

    public void writeFloat(ByteBuf buf, float f) {
        buf.writeInt(Float.floatToIntBits(f));
    }

    public void writeDouble(ByteBuf buf, double d) {
        buf.writeLong(Double.doubleToLongBits(d));
    }

    public void writeBoolean(ByteBuf buf, boolean b) {
        buf.writeBoolean(b);
    }

    public void writeString(ByteBuf buf, String content) {
        writeBytes(buf, content == null ? null : content.getBytes(StandardCharsets.UTF_8));
    }

    public void writeBytes(ByteBuf buf, byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            buf.writeShort(0);
        } else if (bytes.length < Short.MAX_VALUE) {
            buf.writeShort(bytes.length).writeBytes(bytes);
        } else {
            buf.writeShort(Short.MAX_VALUE).writeInt(bytes.length - Short.MAX_VALUE).writeBytes(bytes);
        }
    }

    public byte readByte(ByteBuf buf) {
        return buf.readByte();
    }

    public short readShort(ByteBuf buf) {
        return buf.readShort();
    }

    public int readInt(ByteBuf buf) {
        return buf.readInt();
    }

    public long readLong(ByteBuf buf) {
        return buf.readLong();
    }

    public float readFloat(ByteBuf buf) {
        return Float.intBitsToFloat(buf.readInt());
    }

    public double readDouble(ByteBuf buf) {
        return Double.longBitsToDouble(buf.readLong());
    }

    public boolean readBoolean(ByteBuf buf) {
        return buf.readBoolean();
    }

    public String readString(ByteBuf buf) {
        return new String(readBytes(buf), StandardCharsets.UTF_8);
    }

    public byte[] readBytes(ByteBuf buf) {
        int len = buf.readShort();
        if (len > Short.MAX_VALUE) {
            len += buf.readInt();
        }
        byte[] bytes = new byte[len];
        buf.readBytes(bytes);
        return bytes;
    }

    protected abstract void decodeBody0(ByteBuf buf);

    protected abstract void encodeBody0(ByteBuf buf);

    public byte getFlag() {
        return (byte) (commandType << 3 | (0x07 & compressType));
    }

    public byte getCommandType() {
        return commandType;
    }

    public void setCommandType(byte commandType) {
        this.commandType = commandType;
    }

    public byte getCompressType() {
        return compressType;
    }

    public void setCompressType(byte compressType) {
        this.compressType = compressType;
    }

    public int getSessionId() {
        return sessionId;
    }

    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }
}
