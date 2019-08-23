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
import lombok.Data;

/**
 * @author ukuz90
 */
@Data
public class ErrorMessage extends ByteBufMessage {

    private byte cmd;
    private byte code;
    private String reason;
    private String data;

    public ErrorMessage(Connection connection) {
        super(connection, ERROR.getCmd());
    }

    @Override
    protected void decodeBody0(ByteBuf buf) {
        cmd = readByte(buf);
        code = readByte(buf);
        reason = readString(buf);
        data = readString(buf);
    }

    @Override
    protected void encodeBody0(ByteBuf buf) {
        writeByte(buf, cmd);
        writeByte(buf, code);
        writeString(buf, reason);
        writeString(buf, data);
    }

    private ErrorMessage cmd(byte cmd) {
        this.cmd = cmd;
        return this;
    }

    public ErrorMessage code(ErrorCode code) {
        this.code = code.errorCode;
        return this;
    }

    public ErrorMessage reason(String reason) {
        this.reason = reason;
        return this;
    }

    public static ErrorMessage build(ByteBufMessage message) {
        Assert.notNull(message, "message must not null");
        return new ErrorMessage(message.getConnection()).cmd(message.getCommandType());
    }

    @Override
    public String toString() {
        return "ErrorMessage{" +
                "cmd=" + cmd +
                ", code=" + code +
                ", reason='" + reason + '\'' +
                ", data='" + data + '\'' +
                '}';
    }
}
