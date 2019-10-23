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
import io.github.ukuz.piccolo.api.common.utils.StringUtils;
import io.github.ukuz.piccolo.api.connection.ConnectionManager;
import io.github.ukuz.piccolo.api.exchange.handler.ChannelHandler;
import io.github.ukuz.piccolo.api.exchange.support.PacketToMessageConverter;
import io.github.ukuz.piccolo.api.external.common.Assert;
import io.github.ukuz.piccolo.api.service.discovery.DefaultServiceInstance;
import io.github.ukuz.piccolo.api.service.registry.Registration;
import io.github.ukuz.piccolo.api.spi.SpiLoader;
import io.github.ukuz.piccolo.common.ServiceNames;
import io.github.ukuz.piccolo.common.properties.NetProperties;
import io.github.ukuz.piccolo.common.thread.NamedThreadFactory;
import io.github.ukuz.piccolo.common.thread.ThreadNames;
import io.github.ukuz.piccolo.core.handler.ChannelHandlers;
import io.github.ukuz.piccolo.core.handler.MonitorBytesHandler;
import io.github.ukuz.piccolo.core.handler.MonitorQpsHandler;
import io.github.ukuz.piccolo.core.properties.ThreadProperties;
import io.github.ukuz.piccolo.monitor.MonitorExecutorFactory;
import io.github.ukuz.piccolo.transport.codec.Codec;
import io.github.ukuz.piccolo.transport.codec.MultiPacketCodec;
import io.github.ukuz.piccolo.transport.connection.NettyConnectionManager;
import io.github.ukuz.piccolo.transport.server.NettyServer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.handler.traffic.GlobalChannelTrafficShapingHandler;

import java.net.InetSocketAddress;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * @author ukuz90
 */
public class GatewayServer extends NettyServer {

    private InetSocketAddress address;
    private final ConnectionManager cxnxManager;
    private DefaultServiceInstance serviceInstance;
    private GlobalChannelTrafficShapingHandler trafficShapingHandler;
    private MonitorBytesHandler monitorBytesHandler;
    private MonitorQpsHandler monitorQpsHandler;

    public GatewayServer(PiccoloContext piccoloContext) {
        this(piccoloContext,
                piccoloContext.getProperties(NetProperties.class).getGatewayServer().getBindIp(),
                piccoloContext.getProperties(NetProperties.class).getGatewayServer().getBindPort());
    }

    public GatewayServer(PiccoloContext piccoloContext, String host, int port) {
        this(piccoloContext, ChannelHandlers.newGatewayChannelHandler(piccoloContext), new NettyConnectionManager(), host, port);
    }

    public GatewayServer(PiccoloContext piccoloContext,
                         ChannelHandler channelHandler, ConnectionManager cxnxManager,
                         String host, int port) {
        super(piccoloContext, channelHandler, cxnxManager);
        this.cxnxManager = cxnxManager;
        Assert.isTrue(port >= 0, "port was invalid port: " + port);
        this.address = StringUtils.hasText(host) ? new InetSocketAddress(host, port) : new InetSocketAddress(port);
    }

    @Override
    protected Codec newCodec() {
        return new MultiPacketCodec(SpiLoader.getLoader(PacketToMessageConverter.class).getExtension());
    }

    @Override
    protected void doInit() {
        NetProperties.TrafficNestedProperties traffic = piccoloContext.getProperties(NetProperties.class).getGatewayServerTraffic();
        if (traffic.isEnabled()) {
            ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1, new NamedThreadFactory(ThreadNames.T_TRAFFIC_SHAPING));
            trafficShapingHandler = new GlobalChannelTrafficShapingHandler(executor,
                    traffic.getWriteGlobalLimit(), traffic.getReadGlobalLimit(),
                    traffic.getWriteChannelLimit(), traffic.getReadChannelLimit());
        }

        monitorBytesHandler = new MonitorBytesHandler(getName());
        monitorQpsHandler = new MonitorQpsHandler(getName());
    }

    @Override
    protected void doStartComplete(ServerSocketChannel channel) {
        serviceInstance = DefaultServiceInstance.build()
                .host(piccoloContext.getProperties(NetProperties.class).getPublicIp())
                .port(channel.localAddress().getPort())
                .isPersistent(false)
                .serviceId(ServiceNames.S_GATEWAY);

        if (piccoloContext.getExecutorFactory() instanceof MonitorExecutorFactory) {
            ((MonitorExecutorFactory) piccoloContext.getExecutorFactory()).monitor("gs", this.getWorkerGroup());
        }
    }

    @Override
    protected void initOptions(ServerBootstrap server) {
        super.initOptions(server);
        NetProperties net = piccoloContext.getProperties(NetProperties.class);
        if (net.getGatewayServer().getRcvBuf() > 0) {
            server.childOption(ChannelOption.SO_RCVBUF, net.getGatewayServer().getRcvBuf());
        }
        if (net.getGatewayServer().getSndBuf() > 0) {
            server.childOption(ChannelOption.SO_SNDBUF, net.getGatewayServer().getSndBuf());
        }
        if (net.getGatewayServer().getWriteWaterMarkLow() > 0 && net.getGatewayServer().getWriteWaterMarkHigh() > 0) {
            server.childOption(ChannelOption.WRITE_BUFFER_WATER_MARK,
                    new WriteBufferWaterMark(net.getGatewayServer().getWriteWaterMarkLow(), net.getGatewayServer().getWriteWaterMarkHigh()));
        }
    }

    @Override
    protected void initPipeline(ChannelPipeline pipeline) {
        super.initPipeline(pipeline);
        if (trafficShapingHandler != null) {
            pipeline.addFirst(trafficShapingHandler);
        }

        pipeline.addBefore("decoder", "monitor_bytes", monitorBytesHandler);
        pipeline.addAfter("decoder","monitor_qps", monitorQpsHandler);
    }

    @Override
    protected void doDestroy() {
        cxnxManager.destroy();
    }

    @Override
    protected InetSocketAddress getInetSocketAddress() {
        return address;
    }

    @Override
    public int getWorkerIORatio() {
        return 100;
    }

    @Override
    public int getBossThreadNum() {
        return 1;
    }

    @Override
    public int getWorkerThreadNum() {
        return piccoloContext.getProperties(ThreadProperties.class).getGatewayWorkerThreadNum();
    }

    @Override
    public String getBossThreadName() {
        return ThreadNames.T_GATEWAY_BOSS;
    }

    @Override
    public String getWorkerThreadName() {
        return ThreadNames.T_GATEWAY_WORKER;
    }

    @Override
    public String getId() {
        return null;
    }

    @Override
    public Registration getRegistration() {
        return serviceInstance;
    }

    @Override
    public boolean isSecurity() {
        return false;
    }
}
