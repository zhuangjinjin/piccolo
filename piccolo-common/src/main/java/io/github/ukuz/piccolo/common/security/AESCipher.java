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

import io.github.ukuz.piccolo.api.connection.Cipher;
import io.github.ukuz.piccolo.api.external.common.Assert;
import org.apache.commons.crypto.stream.CryptoInputStream;
import org.apache.commons.crypto.stream.CryptoOutputStream;

import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Properties;
/**
 * @author ukuz90
 */
public class AESCipher implements Cipher {

    private final SecretKeySpec key;
    private final IvParameterSpec iv;

    public static final String KEY_ALGORITHM = "AES";
    public static final String KEY_ALGORITHM_TRANSFORM = "AES/CBC/PKCS5Padding";

    public final byte[] keyB;
    public final byte[] ivB;

    public AESCipher(String key, String iv) {
        this(getUTF8Bytes(key), getUTF8Bytes(iv));
    }

    public AESCipher(byte[] key, byte[] iv) {
        Assert.notNull(key, "key must not empty");
        Assert.notNull(iv, "iv must not empty");
        this.keyB = key;
        this.ivB = iv;
        this.key = new SecretKeySpec(key, KEY_ALGORITHM);
        this.iv = new IvParameterSpec(iv);
    }

    @Override
    public byte[] decrypt(byte[] encryptData) {
        Properties properties = new Properties();
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(encryptData);
             CryptoInputStream cis = new CryptoInputStream(KEY_ALGORITHM_TRANSFORM, properties, inputStream, key, iv)) {

            byte[] decryptedData = new byte[cis.available()];
            int decryptedLen = 0;
            int i;
            while ((i = cis.read(decryptedData, decryptedLen, decryptedData.length - decryptedLen)) > -1) {
                decryptedLen += i;
            }
            return Arrays.copyOf(decryptedData, decryptedLen);
        } catch (IOException e) {
            throw new CryptoException(e);
        }
    }

    @Override
    public byte[] encrypt(byte[] originData) {
        Properties properties = new Properties();
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            try (CryptoOutputStream cos = new CryptoOutputStream(KEY_ALGORITHM_TRANSFORM, properties, outputStream, key, iv)) {
                cos.write(originData);
                cos.flush();
            }
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new CryptoException(e);
        }
    }

    @Override
    public String toString() {
        return toString(keyB) + "," + toString(ivB);
    }

    private String toString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            if (i != 0) {
                sb.append('|');
            }
            sb.append(bytes[i]);
        }
        return sb.toString();
    }

    public static byte[] toArray(String str) {
        String[] arr = str.split("|");
        byte[] bytes = new byte[arr.length];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = Byte.parseByte(arr[i]);
        }
        return bytes;
    }

    private static byte[] getUTF8Bytes(String content) {
        return content.getBytes(StandardCharsets.UTF_8);
    }

}
