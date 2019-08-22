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
import io.github.ukuz.piccolo.api.exchange.support.MultiMessage;
import io.github.ukuz.piccolo.api.exchange.support.PacketToMessageConverter;
import io.netty.buffer.ByteBuf;

/**
 * @author ukuz90
 */
public class MultiPacketCodec extends MessageToPacketCodec {

    public MultiPacketCodec() {
        this(null);
    }

    public MultiPacketCodec(PacketToMessageConverter converter) {
        super(converter);
    }

    public MultiPacketCodec(PacketToMessageConverter converter, PacketCodec packetCodec) {
        super(converter, packetCodec);
    }

    @Override
    public void encode(Connection connection, Object message, ByteBuf out) throws CodecException {
        if (message instanceof MultiMessage) {
            ((MultiMessage) message).forEach(msg -> super.encode(connection, message, out));
        } else {
            super.encode(connection, message, out);
        }
    }

    @Override
    public Object decode(Connection connection, ByteBuf in) throws CodecException {
        MultiMessage multiMessage = null;
        while (in.readableBytes() > 0) {
            int readerIndex = in.readerIndex();
            try {
                Object msg = super.decode(connection, in);
                if (multiMessage == null) {
                    multiMessage = MultiMessage.create();
                }
                multiMessage.addMessage(msg);
            } catch (CodecException e) {
                in.readerIndex(readerIndex);
                if (e instanceof PacketUnknownCodecException || e instanceof PacketSizeLimitCodecException) {
                    throw e;
                } else if (e instanceof PacketNotIntactCodecException) {
                    break;
                }
            }
        }
        return multiMessage;
    }
}
