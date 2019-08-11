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
package io.github.ukuz.piccolo.common.json;

import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author ukuz90
 */
public final class Jsons {

    private static final Logger LOGGER = LoggerFactory.getLogger(Jsons.class);

    private Jsons() {}

    public static String toJson(Object pojo) {
        try {
            return JSON.toJSONString(pojo);
        } catch (Exception e) {
            LOGGER.warn("toJson error, pojo: {} cause: {}", pojo, e);
        }
        return null;
    }

    public static String toJson(Map<String, String> map) {
        if (map == null || map.isEmpty()) {
            return "{}";
        }
        StringBuilder sb = new StringBuilder(64 * map.size());
        sb.append("{");
        Iterator<Map.Entry<String, String>> it = map.entrySet().iterator();
        if (it.hasNext()) {
            append(it.next(), sb);
        }
        while (it.hasNext()) {
            sb.append(",");
            append(it.next(), sb);
        }
        return sb.toString();
    }


    private static void append(Map.Entry<String, String> entry, StringBuilder sb) {
        String key = entry.getKey(), value = entry.getValue();
        if (value == null) {
            value = "";
        }
        sb.append('"').append(key).append('"');
        sb.append(':');
        sb.append('"').append(value).append('"');
    }

    public static <T> T fromJson(String jsonStr, Class<T> clazz) {
        try {
            return JSON.parseObject(jsonStr, clazz);
        } catch (Exception e) {
            LOGGER.warn("fromJson error, jsonStr: {} class: {} cause: {}", jsonStr, clazz, e);
        }
        return null;
    }

    public static <T> T fromJson(byte[] bytes, Class<T> clazz) {
        return fromJson(new String(bytes, StandardCharsets.UTF_8), clazz);
    }

    public static <T> List<T> fromJsonToList(String jsonStr, Class<T> clazz) {
        try {
            return JSON.parseArray(jsonStr, clazz);
        } catch (Exception e) {
            LOGGER.warn("fromJsonToList error, jsonStr: {} class: {} cause: {}", jsonStr, clazz, e);
        }
        return null;
    }

}
