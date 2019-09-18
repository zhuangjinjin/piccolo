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
package io.github.ukuz.piccolo.api;

import io.github.ukuz.piccolo.api.cache.CacheManager;
import io.github.ukuz.piccolo.api.common.Monitor;
import io.github.ukuz.piccolo.api.common.threadpool.ExecutorFactory;
import io.github.ukuz.piccolo.api.config.Environment;
import io.github.ukuz.piccolo.api.config.Properties;
import io.github.ukuz.piccolo.api.configcenter.DynamicConfiguration;
import io.github.ukuz.piccolo.api.id.IdGen;
import io.github.ukuz.piccolo.api.mq.MQClient;
import io.github.ukuz.piccolo.api.service.discovery.ServiceDiscovery;
import io.github.ukuz.piccolo.api.service.registry.ServiceRegistry;

/**
 * @author ukuz90
 */
public interface PiccoloContext {

    Monitor getMonitor();

    ServiceRegistry getServiceRegistry();

    ServiceDiscovery getServiceDiscovery();

    CacheManager getCacheManager();

    MQClient getMQClient();

    Environment getEnvironment();

    <T extends Properties> T getProperties(Class<T> clazz);

    ExecutorFactory getExecutorFactory();

    IdGen getIdGen();

    DynamicConfiguration getDynamicConfiguration();



}
