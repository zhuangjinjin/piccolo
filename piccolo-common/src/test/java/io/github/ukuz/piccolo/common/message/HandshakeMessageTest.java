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
import io.github.ukuz.piccolo.api.connection.SessionContext;
import io.github.ukuz.piccolo.api.exchange.protocol.Packet;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class HandshakeMessageTest {

    @Mock
    private Connection connection;

    @Mock
    private Channel channel;

    @Mock
    private SessionContext sessionContext;

    @DisplayName("test_decode")
    @Test
    void testDecode() {
        when(connection.getChannel()).thenReturn(channel);
        when(connection.getChannel().alloc()).thenReturn(PooledByteBufAllocator.DEFAULT);
        when(connection.getSessionContext()).thenReturn(sessionContext);
        when(connection.getSessionContext().getCipher()).thenReturn(null);

        HandshakeMessage out = new HandshakeMessage(connection);
        out.timestamp = System.currentTimeMillis();
        out.osName = "Android";
        out.osVersion = "11";
        out.clientVersion = "1.0.0";
        out.deviceId = "1w3ex3sd2d3cex2sfw44ew";
        Packet packet = out.encodeBody();
        System.out.println(packet.getPayload().length);

        HandshakeMessage in = new HandshakeMessage(connection);
        in.decodeBody(packet);
        assertEquals("Android", in.osName);
        assertEquals("11", in.osVersion);
        assertEquals("1.0.0", in.clientVersion);
        assertEquals("1w3ex3sd2d3cex2sfw44ew", in.deviceId);
    }

}