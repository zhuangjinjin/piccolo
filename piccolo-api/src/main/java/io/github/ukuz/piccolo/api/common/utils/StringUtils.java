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
package io.github.ukuz.piccolo.api.common.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author ukuz90
 */
public final class StringUtils {

    private StringUtils() {}

    public static boolean hasText(CharSequence str) {
        return hasLength(str) && containText(str);
    }

    private static boolean hasLength(CharSequence str) {
        return str != null && str.length() > 0;
    }

    private static boolean containText(CharSequence str) {
        int strLen = str.length();
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(str.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasText(String str) {
        return hasLength(str) && containText(str);
    }

    private static boolean containText(String str) {
        int strLen = str.length();
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(str.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasLength(String str) {
        return str != null && !str.isEmpty();
    }

    public static boolean equals(String str1, String str2) {
        if (str1 == str2) {
            return true;
        }
        if (str1 == null || str2 == null) {
            return false;
        }
        if (str1.length() != str2.length()) {
            return false;
        }
        return str1.equals(str2);
    }

    public static byte[][] split(byte[] buf, char separator, int segmentNum) {
        if (segmentNum <= 1) {
            return new byte[][]{buf};
        }
        byte[][] list = new byte[segmentNum][];
        int cursor = 0;
        int j = 0;
        for (int i = 0; i < buf.length; i++) {
            if (j == segmentNum - 1) {
                byte[] segment = new byte[buf.length - cursor];
                System.arraycopy(buf, cursor, segment, 0, segment.length);
                list[j] = segment;
                break;
            }
            if (buf[i] == separator) {
                byte[] segment = new byte[i - cursor];
                System.arraycopy(buf, cursor, segment, 0, segment.length);
                list[j] = segment;
                cursor = i + 1;
                j++;
            }
        }
        return list;
    }

    public static List<byte[]> split(byte[] buf, char separator) {
        List<byte[]> list = new ArrayList<>();
        int cursor = 0;
        for (int i = 0; i < buf.length; i++) {
            if (buf[i] == separator) {
                byte[] segment = new byte[i - cursor];
                System.arraycopy(buf, cursor, segment, 0, segment.length);
                list.add(segment);
                cursor = i + 1;
            }
        }
        return list;
    }

    /**
     * Uncapitalize a {@code String}, changing the first letter to
     * lower case as per {@link Character#toLowerCase(char)}.
     * No other letters are changed.
     * @param str
     * @return
     */
    public static String uncapitalize(String str) {
        return changeFirstCharacterCase(str, false);
    }


    private static String changeFirstCharacterCase(String str, boolean capitalize) {
        if (!hasLength(str)) {
            return str;
        }

        char baseChar = str.charAt(0);
        char updatedChar;
        if (capitalize) {
            updatedChar = Character.toUpperCase(baseChar);
        }
        else {
            updatedChar = Character.toLowerCase(baseChar);
        }
        if (baseChar == updatedChar) {
            return str;
        }

        char[] chars = str.toCharArray();
        chars[0] = updatedChar;
        return new String(chars, 0, chars.length);
    }

}
