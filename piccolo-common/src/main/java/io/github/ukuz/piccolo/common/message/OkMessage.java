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

import io.github.ukuz.piccolo.api.external.common.Assert;
import io.github.ukuz.piccolo.common.ErrorCode;
import io.netty.buffer.ByteBuf;

/**
 * @author ukuz90
 */
public class OkMessage extends ByteBufMessage {

    private byte cmd;
    private byte code;
    private String data;

    public OkMessage(Connection connection) {
        super(connection, OK.getCmd());
    }

    @Override
    protected void decodeBody0(ByteBuf buf) {
        cmd = readByte(buf);
        code = readByte(buf);
        data = readString(buf);
    }

    @Override
    protected void encodeBody0(ByteBuf buf) {
        writeByte(buf, cmd);
        writeByte(buf, code);
        writeString(buf, data);
    }

    private OkMessage cmd(byte cmd) {
        this.cmd = cmd;
        return this;
    }

    public OkMessage code(ErrorCode code) {
        this.code = code.errorCode;
        return this;
    }

    public OkMessage data(String data) {
        this.data = data;
        return this;
    }

    public static OkMessage build(ByteBufMessage message) {
        Assert.notNull(message, "message must not null");
        return new OkMessage(message.getConnection()).cmd(message.getCommandType());
    }
}
