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

import java.security.*;
import java.util.Base64;

/**
 * @author ukuz90
 */
public final class RSAGenerator {

    private static final String KEY_ALGORITHM = "RSA";

    private static final int KEY_SIZE = 1024;

    private RSAGenerator() {}

    public static Pair<String, String> generateKeyPair() throws NoSuchAlgorithmException {
        Pair<PublicKey, PrivateKey> pair = generatePair();
        byte[] pubKey = pair.getFirst().getEncoded();
        byte[] priKey = pair.getSecond().getEncoded();
        String publicKey = new String(Base64.getEncoder().encode(pubKey));
        String privateKey = new String(Base64.getEncoder().encode(priKey));
        return Pair.of(publicKey, privateKey);
    }

    public static Pair<PublicKey, PrivateKey> generatePair() throws NoSuchAlgorithmException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance(KEY_ALGORITHM);
        generator.initialize(KEY_SIZE, new SecureRandom());
        KeyPair keyPair = generator.generateKeyPair();
        PublicKey publicKey = keyPair.getPublic();
        PrivateKey privateKey = keyPair.getPrivate();
        return Pair.of(publicKey, privateKey);
    }

    public static void main(String[] args) throws NoSuchAlgorithmException {
        Pair pair = generateKeyPair();
        System.out.println("public key: " + pair.getFirst());
        System.out.println("private key: " + pair.getSecond());
    }

}
