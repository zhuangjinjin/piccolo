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
package io.github.ukuz.piccolo.core.server;

import io.github.ukuz.piccolo.api.PiccoloContext;
import io.github.ukuz.piccolo.api.connection.ConnectionManager;
import io.github.ukuz.piccolo.api.exchange.handler.ChannelHandler;
import io.github.ukuz.piccolo.api.exchange.support.PacketToMessageConverter;
import io.github.ukuz.piccolo.api.external.common.Assert;
import io.github.ukuz.piccolo.api.service.discovery.DefaultServiceInstance;
import io.github.ukuz.piccolo.api.service.discovery.ServiceInstance;
import io.github.ukuz.piccolo.api.service.registry.Registration;
import io.github.ukuz.piccolo.api.spi.SpiLoader;
import io.github.ukuz.piccolo.common.ServiceNames;
import io.github.ukuz.piccolo.common.properties.NetProperties;
import io.github.ukuz.piccolo.common.thread.NamedThreadFactory;
import io.github.ukuz.piccolo.common.thread.ThreadNames;
import io.github.ukuz.piccolo.core.handler.ChannelHandlers;
import io.github.ukuz.piccolo.core.properties.ThreadProperties;
import io.github.ukuz.piccolo.registry.zookeeper.ZKRegistration;
import io.github.ukuz.piccolo.transport.codec.Codec;
import io.github.ukuz.piccolo.transport.codec.MultiPacketCodec;
import io.github.ukuz.piccolo.transport.connection.NettyConnectionManager;
import io.github.ukuz.piccolo.transport.server.NettyServer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.traffic.GlobalChannelTrafficShapingHandler;

import java.net.InetSocketAddress;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * @author ukuz90
 */
public class ConnectServer extends NettyServer {

    private InetSocketAddress address;
    private final String host;
    private final int port;
    private GlobalChannelTrafficShapingHandler channelTrafficShapingHandler;

    public ConnectServer(PiccoloContext piccoloContext) {
        this(piccoloContext,
                piccoloContext.getProperties(NetProperties.class).getConnectServer().getBindIp(),
                piccoloContext.getProperties(NetProperties.class).getConnectServer().getBindPort());
    }

    public ConnectServer(PiccoloContext piccoloContext, String host, int port) {
        this(piccoloContext, ChannelHandlers.newConnectChannelHandler(piccoloContext), new NettyConnectionManager(), host, port);
    }

    public ConnectServer(PiccoloContext piccoloContext,
                         ChannelHandler channelHandler, ConnectionManager cxnxManager,
                         String host, int port) {
        super(piccoloContext, channelHandler, cxnxManager);
        Assert.notEmptyString(host, "host must not be empty");
        Assert.isTrue(port >= 0, "port was invalid port: " + port);
        this.host = host;
        this.port = port;
    }

    @Override
    protected Codec newCodec() {
        return new MultiPacketCodec(SpiLoader.getLoader(PacketToMessageConverter.class).getExtension());
    }

    @Override
    protected void doInit() {
        this.address = new InetSocketAddress(this.host, this.port);
        //限流
        NetProperties.TrafficNestedProperties traffic = piccoloContext.getProperties(NetProperties.class).getConnectServerTraffic();
        if (traffic.isEnabled()) {
            ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1, new NamedThreadFactory(ThreadNames.T_TRAFFIC_SHAPING));
            channelTrafficShapingHandler = new GlobalChannelTrafficShapingHandler(executor,
                    traffic.getWriteGlobalLimit(), traffic.getReadGlobalLimit(),
                    traffic.getWriteChannelLimit(), traffic.getReadChannelLimit());
        }
    }

    @Override
    protected void doDestory() {

    }

    @Override
    protected void initPipeline(ChannelPipeline pipeline) {
        super.initPipeline(pipeline);
        if (channelTrafficShapingHandler != null) {
            pipeline.addFirst(channelTrafficShapingHandler);
        }
    }

    @Override
    protected InetSocketAddress getInetSocketAddress() {
        return address;
    }

    @Override
    public int getWorkerIORatio() {
        return 70;
    }

    @Override
    public int getBossThreadNum() {
        return 1;
    }

    @Override
    public int getWorkerThreadNum() {
        return piccoloContext.getProperties(ThreadProperties.class).getConnectWorkerThreadNum();
    }

    @Override
    public String getBossThreadName() {
        return ThreadNames.T_CONN_BOSS;
    }

    @Override
    public String getWorkerThreadName() {
        return ThreadNames.T_CONN_WORKER;
    }

    @Override
    public String getId() {
        return null;
    }

    @Override
    public Registration getRegistration() {
        ServiceInstance si = DefaultServiceInstance.builder()
                .host(host)
                .port(port)
                .isPersistent(false)
                .serviceId(ServiceNames.S_CONNECT)
                .build();
        return ZKRegistration.build(si);
    }
}
