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
import static io.github.ukuz.piccolo.common.constants.CommandType.ID_GEN;

import io.netty.buffer.ByteBuf;
import lombok.Getter;

import java.util.Arrays;

/**
 * @author ukuz90
 */
@Getter
public class IdGenOkMessage extends ByteBufMessage {

    private long[] xid;
    private byte code;
    private String tag;
    private Long id;

    public IdGenOkMessage(Connection connection) {
        super(connection, ID_GEN.getCmd());
    }

    @Override
    protected void decodeBody0(ByteBuf buf) {
        xid = readLongs(buf);
        code = readByte(buf);
        tag = readString(buf);
        id = readLong(buf);
    }

    @Override
    protected void encodeBody0(ByteBuf buf) {
        writeLongs(buf, xid);
        writeByte(buf, code);
        writeString(buf, tag);
        writeLong(buf, id);
    }

    public static IdGenOkMessage build(Connection connection) {
        return new IdGenOkMessage(connection);
    }

    public IdGenOkMessage xid(long[] xid) {
        this.xid = xid;
        return this;
    }

    public IdGenOkMessage code(byte code) {
        this.code = code;
        return this;
    }

    public IdGenOkMessage tag(String tag) {
        this.tag = tag;
        return this;
    }

    public IdGenOkMessage id(Long id) {
        this.id = id;
        return this;
    }

    @Override
    public String toString() {
        return "IdGenOkMessage{" +
                "xid=" + Arrays.toString(xid) +
                ", code=" + code +
                ", tag='" + tag + '\'' +
                ", id=" + id +
                '}';
    }
}
