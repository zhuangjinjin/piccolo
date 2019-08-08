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
package io.github.ukuz.piccolo.core.session;

import io.github.ukuz.piccolo.api.connection.SessionContext;
import io.github.ukuz.piccolo.common.security.AESCipher;
import lombok.Data;

/**
 * @author ukuz90
 */
@Data
public final class ReusableSession {

    private String sessionId;
    private long expireTime;
    private SessionContext context;

    public static String encode(SessionContext context) {
        StringBuilder sb = new StringBuilder();
        sb.append(context.getOsName()).append(',');
        sb.append(context.getOsVersion()).append(',');
        sb.append(context.getClientVersion()).append(',');
        sb.append(context.getDeviceId()).append(',');
        sb.append(context.getCipher());

        return sb.toString();
    }

    public static ReusableSession decode(String encodeValue) {
        String[] arr = encodeValue.split(",");
        if (arr.length != 7) {
            return null;
        }
        SessionContext context = new SessionContext();
        context.setOsName(arr[0]);
        context.setOsVersion(arr[1]);
        context.setClientVersion(arr[2]);
        context.setDeviceId(arr[3]);
        int keyLength = Integer.parseInt(arr[6]);
        byte[] key = AESCipher.toArray(arr[4], keyLength);
        byte[] iv = AESCipher.toArray(arr[5], keyLength);
        AESCipher aesCipher = new AESCipher(keyLength, key, iv);
        context.changeCipher(aesCipher);
        ReusableSession session = new ReusableSession();
        session.setContext(context);
        return session;
    }

}
