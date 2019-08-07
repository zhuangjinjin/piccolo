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
import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;

class RSACipherTest {

    public static final String PUBLIC_KEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCJRM3ihqaaYE58SiZZq40WfBd3zOQHyvufifV/mZzN+wON7w8dptP1a4KRy3U1IhsXZGYmzol7sJJaz8GKZmznOrkfJ3i/FNXSDRsOtTXq+j1Nn6GjwNtHn7/Oh5Jc87QN+XVT9eFvryXtd88jQ1UMmQ5/nnoOgkGEBYrCWXZXoQIDAQAB";
    public static final String PRIVATE_KEY = "MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBAIlEzeKGpppgTnxKJlmrjRZ8F3fM5AfK+5+J9X+ZnM37A43vDx2m0/VrgpHLdTUiGxdkZibOiXuwklrPwYpmbOc6uR8neL8U1dINGw61Ner6PU2foaPA20efv86HklzztA35dVP14W+vJe13zyNDVQyZDn+eeg6CQYQFisJZdlehAgMBAAECgYBnEkzuPWE4JfyJAzyMtG36Zi9Q+352A4qxHuxCZdwCJutiVhEpmK0raRDf3MAjp3pkNDUoCondYJZkrf+LpqE6WlnxKmCL6bZYDUS72KzyUHh2tySVZPzXi9m2bbMHMazeij9Z/E8Amr+zsomYWX+sjHmDUmKIPVls9zsrOpBDgQJBANqZXScBaTodbgMvJC7Gtcl8ilYclvpIw125v5rEtBx42BwRv6qAUGYEUh9cp0PfOCrvW5Yrk0/aVB4ZixtLtSkCQQCgwSyNL+BjMX0a3b/8Ep0syOyS59Dj5qzKMJRf8O+o5nSDpCsjmSfO43WGOERmy+pcoazfGx82WRu8qKCOtaW5AkADle5sPF6wgFkUnkpWphXHR06xmOh1FIp32Bsne8CEkwrgcv4U2uP2uG1sbEWmHFw8gA6diPtynN8yWzLn8Lb5AkB/vmJX49bNHHONGPBKMMSD5TrR1rNRl2px1b7iqsTYFCI8xgWd6UQQDQLEYQxCIAMiDzwfox7fglofRmUoYsLJAkBf1YTv/uNRo5s9jNCMXOPtEDnh7ukqqEC0IfLtv/5e/twQZRb1zYb1GeqSv8tD80n2SettPB2hoFbwPs4UTH7H";

    private static RSACipher rsaCipher;

    @BeforeAll
    static void setUp() {
        rsaCipher = new RSACipher(PUBLIC_KEY, PRIVATE_KEY);
    }

    @DisplayName("test_decrypt")
    @Test
    void testDecrypt() {
        byte[] encryptData = rsaCipher.encrypt("我是ukuz😄".getBytes(StandardCharsets.UTF_8));
        byte[] decryptData = rsaCipher.decrypt(encryptData);
        assertEquals("我是ukuz😄", new String(decryptData, StandardCharsets.UTF_8));
    }
}