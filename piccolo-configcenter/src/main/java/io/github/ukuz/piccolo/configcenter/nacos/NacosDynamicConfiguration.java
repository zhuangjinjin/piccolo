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
package io.github.ukuz.piccolo.configcenter.nacos;

import com.alibaba.nacos.api.config.ConfigFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.AbstractSharedListener;
import com.alibaba.nacos.api.exception.NacosException;
import io.github.ukuz.piccolo.api.PiccoloContext;
import io.github.ukuz.piccolo.api.config.properties.NacosProperties;
import io.github.ukuz.piccolo.api.configcenter.ConfigurationChangeType;
import io.github.ukuz.piccolo.api.configcenter.ConfigurationChangedEvent;
import io.github.ukuz.piccolo.api.configcenter.ConfigurationListener;

import io.github.ukuz.piccolo.api.configcenter.DynamicConfiguration;
import io.github.ukuz.piccolo.api.service.AbstractService;
import io.github.ukuz.piccolo.api.service.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executor;

/**
 * @author ukuz90
 */
public class NacosDynamicConfiguration extends AbstractService implements DynamicConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(NacosDynamicConfiguration.class);
    private ConfigService configService;
    private static final Long DEFAULT_TIMEOUT = 3000L;
    private final ConcurrentMap<String, NacosConfigurationListener> watchListenerMap = new ConcurrentHashMap<>();

    @Override
    public void init(PiccoloContext context) throws ServiceException {
        NacosProperties prop = context.getProperties(NacosProperties.class);
        try {
            this.configService = ConfigFactory.createConfigService(prop.build());
        } catch (NacosException e) {
            LOGGER.error("nacos init failure, err: {}", e.getCause());
            throw new ServiceException("nacos init failure", e);
        }
    }

    @Override
    public void setProperty(String key, String group, String val) {
        try {
            LOGGER.info("setProperty key: {} val: {}", key, val);
            configService.publishConfig(key, group, val);
        } catch (NacosException e) {
            LOGGER.error("setProperty failure, err: {}", e.getCause());
        }
    }

    @Override
    public void addListener(String key, String group, ConfigurationListener listener) {
        NacosConfigurationListener nacosListener = watchListenerMap.computeIfAbsent(key, k -> createNacosConfigurationListener(k, group));
        nacosListener.addListener(listener);
        try {
            configService.addListener(key, group, nacosListener);
        } catch (NacosException e) {
            LOGGER.error("addListener failure, err: {}", e.getCause());
        }
    }

    @Override
    public void removeListener(String key, String group, ConfigurationListener listener) {
        watchListenerMap.computeIfPresent(key, (k, v) -> {
            v.removeListener(listener);
            return v;
        });
    }

    @Override
    public String getProperty(String key) {
        try {
            String val = configService.getConfig(key, DEFAULT_GROUP, DEFAULT_TIMEOUT);
            LOGGER.info("getProperty key: {} val: {}", key, val);
            return val;
        } catch (NacosException e) {
            LOGGER.error("getProperty failure, err: {}", e.getCause());
        }
        return null;
    }

    private NacosConfigurationListener createNacosConfigurationListener(String key, String group) {
        NacosConfigurationListener nacosListener = new NacosConfigurationListener();
        nacosListener.fillContext(key, group);
        return nacosListener;
    }

    private class NacosConfigurationListener extends AbstractSharedListener {

        private Set<ConfigurationListener> listeners = new CopyOnWriteArraySet<>();
        private Map<String, String> cachedData = new ConcurrentHashMap<>();

        void addListener(ConfigurationListener listener) {
            listeners.add(listener);
        }

        void removeListener(ConfigurationListener listener) {
            listeners.remove(listener);
        }

        @Override
        public Executor getExecutor() {
            return null;
        }

        @Override
        public void innerReceive(String dataId, String group, String configInfo) {
            LOGGER.info("innerReceive data: {}, group: {}, configInfo: {}", dataId, group, configInfo);
            String oldData = cachedData.get(dataId);
            ConfigurationChangedEvent event = new ConfigurationChangedEvent(dataId, configInfo, getChangeType(oldData, configInfo));
            if (configInfo != null) {
                cachedData.put(dataId, configInfo);
            } else {
                cachedData.remove(dataId);
            }
            listeners.forEach(listener -> listener.onConfigurationChanged(event));
        }

        private ConfigurationChangeType getChangeType(String oldData, String newData) {
            if (newData == null) {
                return ConfigurationChangeType.DELETED;
            }
            if (oldData == null) {
                return ConfigurationChangeType.ADDED;
            }
            return ConfigurationChangeType.MODIFIED;
        }
    }
}
