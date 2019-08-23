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

import io.github.ukuz.piccolo.api.connection.ConnectionManager;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;

/**
 * @author ukuz90
 */
public class BinaryFrameDuplexCodec extends WebSocketFrameDuplexCodec<BinaryWebSocketFrame> {

    public BinaryFrameDuplexCodec(ConnectionManager cxnxManager, Codec codec) {
        super(cxnxManager, codec);
    }

    @Override
    BinaryWebSocketFrame wrapFrame(ByteBuf buf) {
        return new BinaryWebSocketFrame(true, 0, buf);
    }

    @Override
    ByteBuf unwrapFrame(BinaryWebSocketFrame frame) {
        return frame.content();
    }

    @Override
    boolean match(Object frame) {
        return frame instanceof BinaryWebSocketFrame;
    }

}
