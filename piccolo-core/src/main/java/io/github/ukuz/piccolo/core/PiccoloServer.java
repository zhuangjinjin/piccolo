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
import io.github.ukuz.piccolo.api.common.threadpool.MonitorExecutorFactory;
import io.github.ukuz.piccolo.api.common.utils.StringUtils;
import io.github.ukuz.piccolo.api.config.Environment;
import io.github.ukuz.piccolo.api.config.Properties;
import io.github.ukuz.piccolo.api.configcenter.DynamicConfiguration;
import io.github.ukuz.piccolo.api.id.IdGen;
import io.github.ukuz.piccolo.api.mq.MQClient;
import io.github.ukuz.piccolo.api.route.RouteLocator;
import io.github.ukuz.piccolo.api.service.ServiceRegistryAndDiscovery;
import io.github.ukuz.piccolo.api.service.discovery.ServiceDiscovery;
import io.github.ukuz.piccolo.api.service.registry.Registration;
import io.github.ukuz.piccolo.api.service.registry.ServiceRegistry;
import io.github.ukuz.piccolo.api.spi.SpiLoader;
import io.github.ukuz.piccolo.common.event.EventBus;
import io.github.ukuz.piccolo.common.properties.CoreProperties;
import io.github.ukuz.piccolo.core.endpoint.SpringConfiguration;
import io.github.ukuz.piccolo.core.id.snowflake.SnowflakeIdGen;
import io.github.ukuz.piccolo.core.id.snowflake.ZooKeeperWorkerIdHolder;
import io.github.ukuz.piccolo.core.router.RouterCenter;
import io.github.ukuz.piccolo.core.server.ConnectServer;
import io.github.ukuz.piccolo.core.server.GatewayServer;
import io.github.ukuz.piccolo.core.server.WebSocketServer;
import io.github.ukuz.piccolo.core.session.ReusableSessionManager;
import io.github.ukuz.piccolo.core.threadpool.ServerExecutorFactory;
import io.github.ukuz.piccolo.monitor.MonitorService;
import org.springframework.boot.Banner;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.web.context.ConfigurableWebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;


/**
 * @author ukuz90
 */
public class PiccoloServer implements PiccoloContext {

    private final Environment environment;
    private final GatewayServer gatewayServer;
    private final ConnectServer connectServer;
    private final WebSocketServer webSocketServer;
    private final ReusableSessionManager reusableSessionManager;
    private final CacheManager cacheManager;
    private final MonitorExecutorFactory executorFactory;
    private final MQClient mqClient;
    private final ServiceRegistryAndDiscovery srd;
    private final DynamicConfiguration configCenter;
    private final Monitor monitor;

    private final RouterCenter routerCenter;
    private final RouteLocator routeLocator;
    private final IdGen idGen;

    public PiccoloServer() {
        //initialize config
        environment = SpiLoader.getLoader(Environment.class).getExtension();
        environment.scanAllProperties();
        environment.load(System.getProperty("piccolo.server.conf", "piccolo-server.properties"));

        CoreProperties core = environment.getProperties(CoreProperties.class);
        //initialize eventBus
        executorFactory = new MonitorExecutorFactory(new ServerExecutorFactory());
        EventBus.create(executorFactory.create(ExecutorFactory.EVENT_BUS, environment));

        String srdChooser = StringUtils.hasText(core.getSrd()) ? core.getSrd() : ServiceRegistryAndDiscovery.DEFAULT;
        srd = SpiLoader.getLoader(ServiceRegistryAndDiscovery.class).getExtension(srdChooser);

        mqClient = SpiLoader.getLoader(MQClient.class).getExtension();

        String configCenterChooser = StringUtils.hasText(core.getConfigCenter()) ? core.getConfigCenter() : DynamicConfiguration.DEFAULT;
        configCenter = SpiLoader.getLoader(DynamicConfiguration.class).getExtension(configCenterChooser);

        reusableSessionManager = new ReusableSessionManager(this);

        cacheManager = SpiLoader.getLoader(CacheManager.class).getExtension();

        routerCenter = new RouterCenter(this);

        routeLocator = SpiLoader.getLoader(RouteLocator.class).getExtension();

        ZooKeeperWorkerIdHolder workerIdHolder = new ZooKeeperWorkerIdHolder(this);
        idGen = new SnowflakeIdGen(workerIdHolder);

        monitor = new MonitorService();

        gatewayServer = new GatewayServer(this);
        connectServer = new ConnectServer(this);
        webSocketServer = new WebSocketServer(this);

    }

    public void runWebServer(String... args) {
        ConfigurableWebApplicationContext context = new AnnotationConfigWebApplicationContext();
        context.refresh();
        context.getBeanFactory().registerSingleton("monitor", monitor);

        SpringApplicationBuilder sab = new SpringApplicationBuilder(SpringConfiguration.class);
        sab.bannerMode(Banner.Mode.OFF);
        sab.parent(context);
        sab.run(args);
    }

    @Override
    public Monitor getMonitor() {
        return monitor;
    }

    @Override
    public ServiceRegistry getServiceRegistry() {
        return srd;
    }

    @Override
    public ServiceDiscovery getServiceDiscovery() {
        return srd;
    }

    @Override
    public CacheManager getCacheManager() {
        return cacheManager;
    }

    @Override
    public MQClient getMQClient() {
        return mqClient;
    }

    @Override
    public Environment getEnvironment() {
        return environment;
    }

    @Override
    public <T extends Properties> T getProperties(Class<T> clazz) {
        return environment.getProperties(clazz);
    }

    @Override
    public MonitorExecutorFactory getExecutorFactory() {
        return executorFactory;
    }

    @Override
    public IdGen getIdGen() {
        return idGen;
    }

    @Override
    public DynamicConfiguration getDynamicConfiguration() {
        return configCenter;
    }

    public GatewayServer getGatewayServer() {
        return gatewayServer;
    }

    public ConnectServer getConnectServer() {
        return connectServer;
    }

    public WebSocketServer getWebSocketServer() {
        return webSocketServer;
    }

    public ReusableSessionManager getReusableSessionManager() {
        return reusableSessionManager;
    }

    public RouterCenter getRouterCenter() {
        return routerCenter;
    }

    public RouteLocator getRouteLocator() {
        return routeLocator;
    }

    public boolean isTargetMachine(String targetAddress, int targetPort) {
        Registration registration = gatewayServer.getRegistration();
        return targetPort == registration.getPort() && StringUtils.equals(targetAddress, registration.getHost());
    }
}
