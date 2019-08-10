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

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;


/**
 * @author ukuz90
 */
public class RSACipher implements Cipher {

    private final PublicKey publicKey;
    private final PrivateKey privateKey;

    public static final String KEY_ALGORITHM = "RSA";

    private final KeyFactory keyFactory;


    public RSACipher(String publicKey, String privateKey) throws CryptoException {
        Assert.notEmptyString(publicKey, "publicKey must not empty");
        Assert.notEmptyString(privateKey, "privateKey must not empty");
        try {
            this.keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
            this.publicKey = newX509EncodedKeySpec(publicKey, keyFactory);
            this.privateKey = newPKCS8EncodedKeySpec(privateKey, keyFactory);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new CryptoException(e);
        }
    }

    public RSACipher(PublicKey publicKey, PrivateKey privateKey, KeyFactory keyFactory) {
        Assert.notNull(publicKey, "publicKey must not null");
        Assert.notNull(privateKey, "privateKey must not null");
        Assert.notNull(keyFactory, "keyFactory must not null");
        this.publicKey = publicKey;
        this.privateKey = privateKey;
        this.keyFactory = keyFactory;
    }

    @Override
    public byte[] decrypt(byte[] encryptData) {
        try {
            javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance(KEY_ALGORITHM);
            cipher.init(javax.crypto.Cipher.DECRYPT_MODE, privateKey);
            byte[] result = cipher.doFinal(Base64.getDecoder().decode(encryptData));
            return result;
        } catch (Exception e) {
            throw new CryptoException(e);
        }
    }

    @Override
    public byte[] encrypt(byte[] originData) {
        try {
            javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance(KEY_ALGORITHM);
            cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, publicKey);
            byte[] result = cipher.doFinal(originData);
            return Base64.getEncoder().encode(result);
        } catch (Exception e) {
            throw new CryptoException(e);
        }
    }

    private PublicKey newX509EncodedKeySpec(String publicKey, KeyFactory keyFactory) throws InvalidKeySpecException {
        byte[] bytes = publicKey.getBytes(StandardCharsets.UTF_8);
        X509EncodedKeySpec pubX509 = new X509EncodedKeySpec(Base64.getDecoder().decode(bytes));
        return keyFactory.generatePublic(pubX509);
    }

    private PrivateKey newPKCS8EncodedKeySpec(String privateKey, KeyFactory keyFactory) throws InvalidKeySpecException {
        byte[] bytes = privateKey.getBytes(StandardCharsets.UTF_8);
        PKCS8EncodedKeySpec priPKCS8 = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(bytes));
        return keyFactory.generatePrivate(priPKCS8);
    }

    @Override
    public String toString() {
        return "RsaCipher [privateKey=" + new String(privateKey.getEncoded()) + ", publicKey=" + new String(publicKey.getEncoded()) + "]";
    }
}
