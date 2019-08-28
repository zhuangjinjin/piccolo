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
package io.github.ukuz.piccolo.common.constants;

/**
 * @author ukuz90
 */
public enum CommandType {

    ERROR(1),
    HANDSHAKE(2),
    HEARTBEAT(3),
    FAST_CONNECT(4),
    BIND_USER(5),
    OK(6),
    DISPATCH(7),
    GATEWAY_PUSH(8),
    KICK(9),
    UNBIND_USER(10),
    ID_GEN(11),

    UNKNOWN(-1);

    private final int cmd;

    CommandType(int cmd) {
        this.cmd = cmd;
    }

    public static CommandType toCMD(byte cmd) {
        CommandType[] values = values();
        if (cmd > 0 && cmd < values.length) {
            return values[cmd - 1];
        }
        return UNKNOWN;
    }

    public byte getCmd() {
        return (byte) cmd;
    }
}
