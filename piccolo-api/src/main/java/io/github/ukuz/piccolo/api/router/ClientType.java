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
package io.github.ukuz.piccolo.api.router;

import java.util.Arrays;

/**
 * @author ukuz90
 */
public enum ClientType {

    MOBILE(1, "android", "ios"),
    WEB(2, "web", "h5"),
    PC(3, "windows", "linux", "mac"),
    UNKNOWN(-1),
    ;

    public final int type;
    public final String[] os;

    ClientType(int type, String... osName) {
        this.type = type;
        this.os = osName;
    }

    boolean contains(String osName) {
        return Arrays.stream(os).anyMatch(osName::contains);
    }

    static ClientType find(String osName) {
        for (ClientType type : ClientType.values()) {
            if (type.contains(osName.toLowerCase())) {
                return type;
            }
        }
        return UNKNOWN;
    }

    static boolean isSameClient(String osNameA, String osNameB) {
        return find(osNameA).contains(osNameB);
    }

}
