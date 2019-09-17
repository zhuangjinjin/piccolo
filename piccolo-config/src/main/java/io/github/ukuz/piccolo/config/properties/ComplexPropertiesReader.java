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
package io.github.ukuz.piccolo.config.properties;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.Reader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.configuration2.PropertiesConfiguration.PropertiesReader;

/**
 * @author ukuz90
 */
public class ComplexPropertiesReader extends PropertiesReader {

    /** Constant for the supported comment characters.*/
    static final String COMMENT_CHARS = "#!";

    /** Constant for the default properties separator.*/
    static final char DEFAULT_SEPARATOR = '=';

    final Stack<Character> bracketStack = new Stack();

    /** Stores the name of the last read property.*/
    private String propertyName;

    /** isComplex of the last read propery.*/
    private boolean complex;

    /** The list of possible key/value separators */
    private static final char[] SEPARATORS = new char[] {'=', ':'};

    /** The regular expression to parse the key and the value of a property. */
    private static final Pattern PROPERTY_PATTERN = Pattern
            .compile("(([\\S&&[^\\\\" + new String(SEPARATORS)
                    + "]]|\\\\.)*)(\\s*(\\s+|[" + new String(SEPARATORS)
                    + "])\\s*)?(.*)");

    private static final String COMPLEX_VALUE_SEPARATOR = "\n";

//    private static final Pattern COMPLEX_KEY_PATTERN = Pattern.compile("");

    public ComplexPropertiesReader(Reader in) {
        super(in);
    }

    @Override
    public String readProperty() throws IOException {
        propertyName = null;
        complex = false;
        StringBuilder sb = new StringBuilder();
        while (true) {
            mark(getLineNumber());
            String line = readLine();

            if (line == null) {
                //EOF
                return sb.length() > 0 ? sb.toString() : null;
            }
            if (isCommentLine(line)) {
                continue;
            }

            line = line.trim();

            if (checkBracketLines(line)) {
                //parse array
                complex = true;
                sb.append(line).append(COMPLEX_VALUE_SEPARATOR);
                String currentPropertyName = fetchLeftBracketName(line);
                if (StringUtils.isNotEmpty(propertyName) && !currentPropertyName.equals(propertyName)) {
                    reset();
                    break;
                }
                propertyName = currentPropertyName;
            } else if (!complex && checkCombineLines(line)) {
                line = line.substring(0, line.length() - 1);
                sb.append(line);
            } else if (!complex) {
                sb.append(line);
                break;
            } else {
                reset();
                break;
            }
        }
        return sb.toString();
    }

    @Override
    protected void parseProperty(String line) {
        if (!complex) {
            super.parseProperty(line);
        } else {
            String[] property = doParseComplexProperty(line);
            initPropertyName(property[0]);
            initPropertyValue(property[1]);
            initPropertySeparator(property[2]);
        }
    }

    String[] doParseComplexProperty(String line) {
        String[] result = new String[]{"", "", ""};
        String[] lineArr = line.split(COMPLEX_VALUE_SEPARATOR);
        StringBuilder jsonValueString = new StringBuilder();
        LinkedHashMap<Integer, Map<String, String>> tmpPropertyValue = new LinkedHashMap<>();
        for (String singleLine : lineArr) {

            final Matcher matcher = PROPERTY_PATTERN.matcher(singleLine);
            if (matcher.matches()) {
                String pName = matcher.group(1).trim();
                String pValue = matcher.group(5);
                result[2] = matcher.group(3);

                //TODO regex parse
                String subName = fetchRightBracketName(pName);
                int index = fetchMiddleBracketIndex(pName);
                if (StringUtils.isNotEmpty(subName) && index >= 0) {
                    tmpPropertyValue.computeIfAbsent(index, (k) -> new HashMap<>());
                    tmpPropertyValue.get(index).put(subName, pValue);
                }
            }

        }
        jsonValueString.append("[");
        tmpPropertyValue.forEach((i, map) -> {
            jsonValueString.append(JSON.toJSON(map));
            if (i != tmpPropertyValue.size() - 1) {
                jsonValueString.append(",");
            }
        });
        jsonValueString.append("]");
        result[0] = propertyName;
        result[1] = jsonValueString.toString();
        return result;
    }

    static boolean isCommentLine(final String line) {
        final String s = line.trim();
        // blank lines are also treated as comment lines
        return s.length() < 1 || COMMENT_CHARS.indexOf(s.charAt(0)) >= 0;
    }

    static boolean checkCombineLines(final String line) {
        return countTrailingBS(line) % 2 != 0;
    }

    boolean checkBracketLines(final String line) {
        bracketStack.clear();
        final String s = line.trim();
        for (int idx = 0; idx < s.length() && line.charAt(idx) != DEFAULT_SEPARATOR; idx++) {
            if (line.charAt(idx) == ']' && bracketStack.peek().equals('[')) {
                bracketStack.push(line.charAt(idx));
            }
            if (line.charAt(idx) == '[' && bracketStack.isEmpty()) {
                bracketStack.push(line.charAt(idx));
            }
        }
        return bracketStack.size() > 0 && bracketStack.size() % 2 == 0;
    }

    String fetchLeftBracketName(final String line) {
        final String s = line.trim();
        return s.substring(0, s.indexOf('['));
    }

    String fetchRightBracketName(final String line) {
        final String s = line.trim();
        int idx = s.indexOf("].");
        return idx > 0 ? s.substring(idx + 2) : "";
    }

    int fetchMiddleBracketIndex(final String line) {
        final String s = line.trim();
        int lIdx = s.indexOf("[");
        int rIdx = s.indexOf("]");
        return Integer.parseInt(s.substring(lIdx + 1, rIdx));
    }

    private static int countTrailingBS(final String line) {
        int bsCount = 0;
        for (int idx = line.length() - 1; idx >= 0 && line.charAt(idx) == '\\'; idx--){
            bsCount++;
        }
        return bsCount;
    }

}
