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

/**
 * @author ukuz90
 */
public class BindUserMessage extends ByteBufMessage {

    public String userId;
    public String tags;
    public String data;

    public BindUserMessage(Connection connection) {
        super(connection, BIND_USER.getCmd());
    }

    @Override
    protected void decodeBody0(ByteBuf buf) {
        userId = readString(buf);
        tags = readString(buf);
        data = readString(buf);
    }

    @Override
    protected void encodeBody0(ByteBuf buf) {
        writeString(buf, userId);
        writeString(buf, tags);
        writeString(buf, data);
    }

    @Override
    public String toString() {
        return "BindUserMessage{" +
                "userId='" + userId + '\'' +
                ", tags='" + tags + '\'' +
                ", data='" + data + '\'' +
                '}';
    }
}
