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
import io.netty.handler.codec.http.websocketx.WebSocketFrame;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ukuz90
 */
public abstract class WebSocketFrameDuplexCodec<T extends WebSocketFrame> {

    private Codec codec;
    private final ConnectionManager cxnxManager;

    private final Encoder encoder = new Encoder();
    private final Decoder decoder = new Decoder();

    public WebSocketFrameDuplexCodec(ConnectionManager cxnxManager, Codec codec) {
        this.cxnxManager = cxnxManager;
        this.codec = codec;
    }

    public Encoder getEncoder() {
        return encoder;
    }

    public Decoder getDecoder() {
        return decoder;
    }

    private class Encoder extends MessageToMessageEncoder<BaseMessage> {

        @Override
        protected void encode(ChannelHandlerContext ctx, BaseMessage msg, List out) throws Exception {
            Connection connection = cxnxManager.getConnection(ctx.channel());
            if (connection != null) {
                ByteBuf buf = ctx.alloc().ioBuffer();
                codec.encode(connection, msg, buf);
                if (buf.isReadable()) {
                    out.add(wrapFrame(buf));
                }
            }
        }
    }

    private class Decoder extends ByteToMessageDecoder {

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            if (match(msg)) {
                List<Object> out = new ArrayList<>();
                decode(ctx, unwrapFrame((T) msg), out);
                out.forEach(ctx::fireChannelRead);
            } else {
                ctx.fireChannelRead(msg);
            }
        }

        @Override
        protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
            Connection connection = cxnxManager.getConnection(ctx.channel());
            Object msg = codec.decode(connection, in);
            if (msg != null) {
                out.add(msg);
            }
        }
    }

    abstract T wrapFrame(ByteBuf buf);

    abstract ByteBuf unwrapFrame(T frame);

    abstract boolean match(Object frame);

}
