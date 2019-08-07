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

import java.security.SecureRandom;

/**
 * @author ukuz90
 */
public final class CipherBox {

    private CipherBox() {}

    private final int aesKeyLength = 16;

    public static final CipherBox I = new CipherBox();

    private SecureRandom random = new SecureRandom();

    public byte[] randomAESKey() {
        byte[] bytes = new byte[aesKeyLength];
        random.nextBytes(bytes);
        return bytes;
    }

    public byte[] mixKey(byte[] clientKey, byte[] serverKey) {
        byte[] bytes = new byte[aesKeyLength];
        for (int i = 0; i < aesKeyLength; i++) {
            int a = clientKey[i];
            int b = serverKey[i];
            int sum = Math.abs(a + b);
            bytes[i] = (byte) ((sum & 0x1) == 0 ? a ^ b : b ^ a);
        }
        return bytes;
    }

    public int getAesKeyLength() {
        return aesKeyLength;
    }
}
