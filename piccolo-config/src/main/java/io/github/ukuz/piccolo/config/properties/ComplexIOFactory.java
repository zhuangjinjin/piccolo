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

import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.convert.ListDelimiterHandler;
import static org.apache.commons.configuration2.PropertiesConfiguration.IOFactory;

import java.io.Reader;
import java.io.Writer;

/**
 * @author ukuz90
 */
public class ComplexIOFactory implements IOFactory {
    @Override
    public PropertiesConfiguration.PropertiesReader createPropertiesReader(Reader in) {
        return new ComplexPropertiesReader(in);
    }

    @Override
    public PropertiesConfiguration.PropertiesWriter createPropertiesWriter(Writer out, ListDelimiterHandler handler) {
        return new PropertiesConfiguration.PropertiesWriter(out, handler);
    }
}
