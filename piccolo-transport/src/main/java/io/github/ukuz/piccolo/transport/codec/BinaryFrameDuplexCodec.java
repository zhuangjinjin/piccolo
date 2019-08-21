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
import io.github.ukuz.piccolo.api.exchange.support.BaseMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ukuz90
 */
public class BinaryFrameDuplexCodec {

    private Codec codec;
    private final ConnectionManager cxnxManager;

    private final Encoder encoder = new Encoder();
    private final Decoder decoder = new Decoder();

    public BinaryFrameDuplexCodec(ConnectionManager cxnxManager, Codec codec) {
        this.cxnxManager = cxnxManager;
        this.codec = codec;
    }

    public BinaryFrameDuplexCodec.Encoder getEncoder() {
        return encoder;
    }

    public BinaryFrameDuplexCodec.Decoder getDecoder() {
        return decoder;
    }

    //    @ChannelHandler.Sharable
    private class Encoder extends MessageToMessageEncoder<BaseMessage> {

        @Override
        protected void encode(ChannelHandlerContext ctx, BaseMessage msg, List out) throws Exception {
            Connection connection = cxnxManager.getConnection(ctx.channel());
            if (connection != null) {
                ByteBuf buf = ctx.alloc().ioBuffer();
                codec.encode(connection, msg, buf);
                if (buf.isReadable()) {
                    BinaryWebSocketFrame frame = new BinaryWebSocketFrame(false, 0, buf);
                    out.add(frame);
                }
            }
        }
    }

    private class Decoder extends ByteToMessageDecoder {

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            if (msg instanceof BinaryWebSocketFrame) {
                List<Object> out = new ArrayList<>();
                decode(ctx, ((BinaryWebSocketFrame) msg).content(), out);
                out.forEach(ctx::fireChannelRead);
            }
        }

        @Override
        protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
            if (ctx.pipeline().get("WS403Responder") == null) {
                return;
            }
            Connection connection = cxnxManager.getConnection(ctx.channel());
            Object msg = codec.decode(connection, in);
            if (msg != null) {
                out.add(msg);
            }
        }
    }
}
