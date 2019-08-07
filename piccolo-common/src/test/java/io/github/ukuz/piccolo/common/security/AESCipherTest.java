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

package io.github.ukuz.piccolo.common.security;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class AESCipherTest {

    private static AESCipher aesCipher;

    @BeforeAll
    static void setUp() {
        aesCipher = new AESCipher("1234567890123456", "1234567890123456");
    }

    @DisplayName("test_decrypt")
    @Test
    void testDecrypt() {
        byte[] encryptData = aesCipher.encrypt("我叫ukuz!".getBytes(StandardCharsets.UTF_8));

        byte[] decryptData = aesCipher.decrypt(encryptData);

        assertEquals("我叫ukuz!", new String(decryptData));
    }

}