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
package io.github.ukuz.piccolo.api.protocol;

import io.netty.buffer.Unpooled;
import lombok.Data;

/**
 *
 * +-----------+----------+---------------+---------+------------+---------+
 * | magic(2B) | flag(1B) | sessionId(4B) | lrc(1B) | length(4B) | payload |
 * | 0xBCC0    | 0x80     |               |         | 0x00000005 | "HELLO" |
 * +-----------+----------+---------------+---------+------------+---------+
 * @author ukuz90
 */
@Data
public class Packet {

    private static final int HEADER_LENGTH = 12;

    /**
     * 魔数
     */
    transient private short magic;
    /**
     * 高5位存cmd，低3位存压缩类型（默认不压缩）
     */
    private byte flag;
    /**
     * 会话ID
     */
    private int sessionId;
    /**
     * 纵向冗余校验
     */
    transient private byte lrc;
    /**
     * payload长度
     */
    private int length;
    /**
     *
     */
    private byte[] payload;

    public byte calcLrc() {
        byte lrc = 0;
        byte[] data = Unpooled.buffer(HEADER_LENGTH - 1)
                .writeShort(magic)
                .writeByte(flag)
                .writeInt(sessionId)
                .writeInt(length)
                .array();
        for (int i = 0; i < data.length; i++) {
            lrc ^= data[i];
        }
        return lrc;
    }

}
