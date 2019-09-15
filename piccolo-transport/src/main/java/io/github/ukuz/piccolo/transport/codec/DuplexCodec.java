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
import io.github.ukuz.piccolo.api.connection.ConnectionManager;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


/**
 * @author ukuz90
 */
public class DuplexCodec {

    private static final Logger LOGGER = LoggerFactory.getLogger(DuplexCodec.class);
    private Codec codec;
    private final ConnectionManager cxnxManager;

    private final Encoder encoder = new Encoder();
    private final Decoder decoder = new Decoder();

    public DuplexCodec(ConnectionManager cxnxManager, Codec codec) {
        this.cxnxManager = cxnxManager;
        this.codec = codec;
    }

    public Encoder getEncoder() {
        return encoder;
    }

    public Decoder getDecoder() {
        return decoder;
    }

//    @ChannelHandler.Sharable
    private class Encoder extends MessageToByteEncoder {

        @Override
        protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) {
            Connection connection = cxnxManager.getConnection(ctx.channel());
            codec.encode(connection, msg, out);
        }
    }

    private class Decoder extends ByteToMessageDecoder {

        @Override
        protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
            Connection connection = cxnxManager.getConnection(ctx.channel());
            try {
                Object msg = codec.decode(connection, in);
                if (msg != null) {
                    out.add(msg);
                }
            } catch (PacketUnknownCodecException | PacketSizeLimitCodecException e) {
                LOGGER.error("Duplex decode failure, cause: {}", e);
                ctx.pipeline().close();
            } catch (CodecException e) {
            }

        }
    }
}
