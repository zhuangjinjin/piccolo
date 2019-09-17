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

import io.github.ukuz.piccolo.config.common.ConfigurationFileNotSupportException;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.resolver.CatalogResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author ukuz90
 */
public class PropertiesConfigurations {

    public static final String PROPERTIES_EXTENSION = ".properties";

    private static final Logger LOGGER = LoggerFactory.getLogger(PropertiesConfigurations.class);

    public Configuration create(String fileName) throws ConfigurationFileNotSupportException {
        Configuration configuration = null;
        try {
            if (fileName.lastIndexOf(PROPERTIES_EXTENSION) != -1) {

                Parameters params = new Parameters();
                FileBasedConfigurationBuilder<PropertiesConfiguration> builder = new FileBasedConfigurationBuilder<>(PropertiesConfiguration.class)
                        .configure(params
                                .properties()
                                .setIOFactory(new ComplexIOFactory())
                                .setFileName(fileName)
                        );
                configuration = builder.getConfiguration();

            } else {
                LOGGER.error("Not support extension, fileName: {}", fileName);
                throw new ConfigurationFileNotSupportException("Not support extension, fileName: " + fileName);
            }
        } catch (ConfigurationException e) {
            LOGGER.error("createConfiguration error: {}", e.getCause());
            throw new ConfigurationFileNotSupportException(e);
        }
        return configuration;
    }

}
