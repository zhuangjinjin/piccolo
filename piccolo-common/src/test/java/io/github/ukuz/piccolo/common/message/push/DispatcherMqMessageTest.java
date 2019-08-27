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
package io.github.ukuz.piccolo.common.message.push;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class DispatcherMqMessageTest {

    @Test
    void doDecode() {
        long xid = 1000L;
        String msg = "你好，piccolo";
        DispatcherMqMessage encodeMsg = new DispatcherMqMessage();
        encodeMsg.setXid(xid);
        encodeMsg.setPayload(msg.getBytes(StandardCharsets.UTF_8));
        byte[] buf = encodeMsg.encode();

        DispatcherMqMessage decodeMsg = new DispatcherMqMessage();
        decodeMsg.decode(buf);
        assertEquals(xid, decodeMsg.getXid());
        assertEquals(msg, new String(decodeMsg.getPayload(), StandardCharsets.UTF_8));
    }
}