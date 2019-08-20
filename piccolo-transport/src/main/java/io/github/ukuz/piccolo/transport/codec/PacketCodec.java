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
package io.github.ukuz.piccolo.transport.codec;

import io.github.ukuz.piccolo.api.connection.Connection;
import io.github.ukuz.piccolo.api.exchange.protocol.Packet;
import io.netty.buffer.ByteBuf;

/**
 * @author ukuz90
 *
 * @see io.github.ukuz.piccolo.api.exchange.protocol.Packet
 */
public class PacketCodec implements Codec {

    private final static int PAYLOAD_MAX_LENGTH = 10 * 1024 * 1024;
    private final static int HEADER_LENGTH = 13;
    private final static int MAGIC_NUM = 0xbcc0;
    private final static byte MAGIC_NUM_H = (byte) (MAGIC_NUM >> 8);
    private final static byte MAGIC_NUM_L = (byte) MAGIC_NUM;

    private Codec codec;

    @Override
    public void encode(Connection connection, Object msg, ByteBuf out) throws CodecException {
        if (msg instanceof Packet) {
            Packet packet = (Packet) msg;
            out.writeByte(MAGIC_NUM_H);
            out.writeByte(MAGIC_NUM_L);
            out.writeByte(packet.getCmd());
            out.writeByte(packet.getFlag());
            out.writeInt(packet.getSessionId());
            out.writeByte(packet.getLrc());
            out.writeInt(packet.getLength());
            out.writeBytes(packet.getPayload());
        } else {
            throw new MessageUnknownCodecException("packet unknown, msg: " + msg.getClass());
        }
    }

    @Override
    public Object decode(Connection connection, ByteBuf in) throws CodecException {
        byte mh = in.readByte();
        byte ml = in.readByte();
        if (mh != MAGIC_NUM_H || ml != MAGIC_NUM_L) {
            throw new PacketUnknownCodecException("packet unknown, mh: " + mh + ", ml: " + ml);
        }

        if (in.readableBytes() + 2 < HEADER_LENGTH) {
            throw new PacketNotIntactCodecException("packet was not intact, can not read intact header, readable len: " + in.readableBytes() + 2);
        }

        //begin
        byte cmd = in.readByte();
        byte flag = in.readByte();
        int sessionId = in.readInt();
        byte lrc = in.readByte();
        int length = in.readInt();

        if (length > PAYLOAD_MAX_LENGTH) {
            throw new PacketSizeLimitCodecException("packet was beyond the payload size limit, payload's len: " + length);
        }

        if (length > in.readableBytes()) {
            throw new PacketNotIntactCodecException("packet was not intact payload's len: " + length + ", but readable len: " + in.readableBytes());
        }

        byte[] payload = new byte[length];
        in.readBytes(payload);

        Packet packet = new Packet();
        packet.setMagic((short)MAGIC_NUM);
        packet.setCmd(cmd);
        packet.setFlag(flag);
        packet.setSessionId(sessionId);
        packet.setLrc(lrc);
        packet.setLength(length);
        packet.setPayload(payload);

        return packet;
    }
}
