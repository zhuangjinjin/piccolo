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
import io.github.ukuz.piccolo.common.properties.SecurityProperties;
import org.apache.commons.crypto.cipher.CryptoCipher;
import org.apache.commons.crypto.utils.Utils;
import sun.nio.ch.DirectBuffer;

import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * @author ukuz90
 */
public class AESCipher implements Cipher {

    private final SecretKeySpec key;
    private final IvParameterSpec iv;

    public static final String KEY_ALGORITHM = "AES";
    public static final String KEY_ALGORITHM_TRANSFORM = "AES/CBC/PKCS5Padding";

    private static final int AES_KEY_LENGTH = 1024;

    public final int keyLength;
    public final byte[] keyB;
    public final byte[] ivB;

    public AESCipher(String key, String iv) {
        this(0, getUTF8Bytes(key), getUTF8Bytes(iv));
    }

    public AESCipher(SecurityProperties security, byte[] key, byte[] iv) {
        this(security.getAesKeyLength(), key, iv);
    }

    public AESCipher(int keyLength, byte[] key, byte[] iv) {
        Assert.notNull(key, "key must not empty");
        Assert.notNull(iv, "iv must not empty");
        this.keyB = key;
        this.ivB = iv;
        this.key = new SecretKeySpec(key, KEY_ALGORITHM);
        this.iv = new IvParameterSpec(iv);
        if (keyLength <= 0) {
            this.keyLength = AES_KEY_LENGTH;
        } else {
            this.keyLength = keyLength;
        }
    }

    @Override
    public byte[] decrypt(byte[] encryptData) {
        Properties properties = new Properties();
        ByteBuffer in = null;
        ByteBuffer out = null;
        try (CryptoCipher decipher = Utils.getCipherInstance(KEY_ALGORITHM_TRANSFORM, properties)) {
            out = ByteBuffer.allocateDirect(keyLength);
            in = ByteBuffer.allocateDirect(encryptData.length);
//            out = PooledByteBufAllocator.DEFAULT.directBuffer(keyLength).nioBuffer();

            in.put(encryptData);
            in.flip();

            decipher.init(javax.crypto.Cipher.DECRYPT_MODE, key, iv);
            decipher.update(in, out);
            decipher.doFinal(in, out);

            out.flip();
            byte[] result = new byte[out.remaining()];
            out.duplicate().get(result);

            return result;

        } catch (Exception e) {
            throw new CryptoException(e);
        } finally {
            close(in);
            close(out);
        }
    }

    @Override
    public byte[] encrypt(byte[] originData) {
        Properties properties = new Properties();
        ByteBuffer in = null;
        ByteBuffer out = null;
        try (CryptoCipher encipher = Utils.getCipherInstance(KEY_ALGORITHM_TRANSFORM, properties)) {
            in = ByteBuffer.allocateDirect(keyLength);
            out = ByteBuffer.allocateDirect(keyLength);

            in.put(originData);
            in.flip();

            encipher.init(javax.crypto.Cipher.ENCRYPT_MODE, key, iv);
            int updateBytes = encipher.update(in, out);
            int finalBytes = encipher.doFinal(in, out);
            byte[] result = new byte[updateBytes + finalBytes];
            out.flip();
            out.duplicate().get(result);

            return result;
        } catch (Exception e) {
            throw new CryptoException(e);
        } finally {
            close(in);
            close(out);
        }

    }

    @Override
    public String toString() {
        return toString(keyB) + "," + toString(ivB) + "," + String.valueOf(keyLength);
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

    public static byte[] toArray(String str, int keyLength) {
        String[] arr = str.split("|");
        if (arr.length != keyLength) {
            return null;
        }
        byte[] bytes = new byte[arr.length];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = Byte.parseByte(arr[i]);
        }
        return bytes;
    }

    private static byte[] getUTF8Bytes(String content) {
        return content.getBytes(StandardCharsets.UTF_8);
    }

    private void close(ByteBuffer buffer) {
        if (buffer instanceof DirectBuffer) {
            ((DirectBuffer) buffer).cleaner().clean();
        }
    }

}
