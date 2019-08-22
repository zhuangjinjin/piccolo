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
import io.github.ukuz.piccolo.common.json.Jsons;
import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;

/**
 * @author ukuz90
 *
 * @see Packet
 */
public class JsonPacketCodec implements PacketCodec {

    private final static int MAGIC_NUM = 0xbcc0;

    @Override
    public void encode(Connection connection, Object msg, ByteBuf out) throws CodecException {
        if (msg instanceof Packet) {
            Packet packet = (Packet) msg;
            packet.setMagic((short) MAGIC_NUM);
            String jsonStr = Jsons.toJson((packet));
            out.writeBytes(jsonStr.getBytes(StandardCharsets.UTF_8));
        } else {
            throw new MessageUnknownCodecException("packet unknown, msg: " + msg.getClass());
        }
    }

    @Override
    public Object decode(Connection connection, ByteBuf in) throws CodecException {
        String text = in.toString(StandardCharsets.UTF_8);
        Packet packet = Jsons.fromJson(text, Packet.class);

        return packet;
    }
}
