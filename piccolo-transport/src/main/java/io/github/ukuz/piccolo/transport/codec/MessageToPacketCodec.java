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
import io.github.ukuz.piccolo.api.exchange.support.BaseMessage;
import io.github.ukuz.piccolo.api.exchange.support.PacketToMessageConverter;
import io.netty.buffer.ByteBuf;

/**
 * @author ukuz90
 */
public class MessageToPacketCodec implements Codec {

    private final PacketToMessageConverter converter;
    private final PacketCodec packetCodec;

    public MessageToPacketCodec(PacketToMessageConverter converter) {
        this(converter, new BinaryPacketCodec());
    }

    public MessageToPacketCodec(PacketToMessageConverter converter, PacketCodec packetCodec) {
        this.converter = converter;
        this.packetCodec = packetCodec;
    }

    @Override
    public void encode(Connection connection, Object msg, ByteBuf out) throws CodecException {
        if (msg instanceof BaseMessage) {
            Packet packet = ((BaseMessage) msg).encodeBody();
            packetCodec.encode(connection, packet, out);
        } else {
            packetCodec.encode(connection, msg, out);
        }
    }

    @Override
    public Object decode(Connection connection, ByteBuf in) throws CodecException {
        Packet packet = (Packet) packetCodec.decode(connection, in);
        if (converter != null) {
            BaseMessage message = converter.convert(packet, connection);
            if (message == null) {
                throw new PacketToMessageNotMappingCodecException("packet not mapping message, cmd: " + packet.getCommandType());
            }
            message.decodeBody(packet);
            return message;
        }
        return packet;
    }
}
