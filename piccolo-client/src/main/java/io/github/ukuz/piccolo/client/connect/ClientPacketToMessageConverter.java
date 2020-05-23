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
package io.github.ukuz.piccolo.client.connect;

import io.github.ukuz.piccolo.api.connection.Connection;
import io.github.ukuz.piccolo.api.exchange.protocol.Packet;
import io.github.ukuz.piccolo.api.exchange.support.BaseMessage;
import io.github.ukuz.piccolo.api.exchange.support.PacketToMessageConverter;
import io.github.ukuz.piccolo.common.constants.CommandType;
import io.github.ukuz.piccolo.common.message.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ukuz90
 */
public class ClientPacketToMessageConverter implements PacketToMessageConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientPacketToMessageConverter.class);

    @Override
    public BaseMessage convert(Packet packet, Connection connection) {
        CommandType cmd = CommandType.toCMD(packet.getCommandType());
        switch (cmd) {
            case HANDSHAKE:
                return new HandshakeOkMessage(connection);
            case FAST_CONNECT:
                return new FastConnectOkMessage(connection);
            case OK:
                return new OkMessage(connection);
            case HEARTBEAT:
                return new HeartbeatMessage(connection);
            case ERROR:
                return new ErrorMessage(connection);
            case ID_GEN:
                return new IdGenOkMessage(connection);
            case DISPATCH:
                return new DispatcherResponseMessage(connection);
            default:
                LOGGER.error("packet covert failure, not found mapping cmd: {} packet: {} conn: {}", cmd, packet, connection);
                break;
        }
        return null;
    }
}
