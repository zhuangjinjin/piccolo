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
package io.github.ukuz.piccolo.registry.nacos.manager;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import io.github.ukuz.piccolo.api.config.properties.NacosProperties;
import io.github.ukuz.piccolo.api.external.common.Assert;
import io.github.ukuz.piccolo.api.service.AbstractService;
import io.github.ukuz.piccolo.api.service.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ukuz90
 */
public class NacosManager extends AbstractService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NacosManager.class);
    private NamingService namingService;
    private NacosDirectory directory;
    private final NacosProperties prop;
    private final String watchPath;

    public NacosManager(NacosProperties prop, String watchPath) {
        Assert.notNull(prop, "nacosProperties must not be null.");
        Assert.notEmptyString(watchPath, "watchPath must not be empty.");
        this.prop = prop;
        this.watchPath = watchPath;
    }

    @Override
    public void init() throws ServiceException {
        try {
            namingService = NamingFactory.createNamingService(prop.build());
        } catch (NacosException e) {
            LOGGER.error("nacos init failure, err: {}", e.getCause());
            throw new ServiceException("nacos init failure", e);
        }
        directory = new NacosDirectory(namingService, watchPath);
    }

    public NacosDirectory getDirectory() {
        return directory;
    }
}
