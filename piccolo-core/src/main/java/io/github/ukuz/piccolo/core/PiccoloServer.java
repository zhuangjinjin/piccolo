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
package io.github.ukuz.piccolo.core;

import io.github.ukuz.piccolo.api.PiccoloContext;
import io.github.ukuz.piccolo.api.cache.CacheManager;
import io.github.ukuz.piccolo.api.common.Monitor;
import io.github.ukuz.piccolo.api.common.threadpool.ExecutorFactory;
import io.github.ukuz.piccolo.api.config.Environment;
import io.github.ukuz.piccolo.api.config.Properties;
import io.github.ukuz.piccolo.api.mq.MQClient;
import io.github.ukuz.piccolo.api.service.discovery.ServiceDiscovery;
import io.github.ukuz.piccolo.api.service.registry.ServiceRegistry;
import io.github.ukuz.piccolo.api.spi.SpiLoader;
import io.github.ukuz.piccolo.common.event.EventBus;
import io.github.ukuz.piccolo.core.server.ConnectServer;
import io.github.ukuz.piccolo.core.server.GatewayServer;
import io.github.ukuz.piccolo.core.session.ReusableSessionManager;
import io.github.ukuz.piccolo.core.threadpool.ServerExecutorFactory;

/**
 * @author ukuz90
 */
public class PiccoloServer implements PiccoloContext {

    private final Environment environment;
    private final GatewayServer gatewayServer;
    private final ConnectServer connectServer;
    private final ReusableSessionManager reusableSessionManager;

    public PiccoloServer() {
        //initialize config
        environment = SpiLoader.getLoader(Environment.class).getExtension();
        environment.scanAllProperties();
        environment.load("piccolo-server.properties");

        //initialize eventBus
        ServerExecutorFactory factory = new ServerExecutorFactory();
        EventBus.create(factory.create(ExecutorFactory.EVENT_BUS, environment));

        reusableSessionManager = new ReusableSessionManager(this);
        gatewayServer = new GatewayServer(this);
        connectServer = new ConnectServer(this);
    }

    @Override
    public Monitor getMonitor() {
        return null;
    }

    @Override
    public ServiceRegistry getServiceRegistry() {
        return null;
    }

    @Override
    public ServiceDiscovery getServiceDiscovery() {
        return null;
    }

    @Override
    public CacheManager getCacheManager() {
        return null;
    }

    @Override
    public MQClient getMQClient() {
        return null;
    }

    @Override
    public Environment getEnvironment() {
        return environment;
    }

    @Override
    public <T extends Properties> T getProperties(Class<T> clazz) {
        return environment.getProperties(clazz);
    }

    public GatewayServer getGatewayServer() {
        return gatewayServer;
    }

    public ConnectServer getConnectServer() {
        return connectServer;
    }

    public ReusableSessionManager getReusableSessionManager() {
        return reusableSessionManager;
    }
}
