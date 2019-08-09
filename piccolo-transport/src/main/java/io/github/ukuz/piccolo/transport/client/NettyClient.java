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
package io.github.ukuz.piccolo.transport.client;

import io.github.ukuz.piccolo.api.config.Environment;
import io.github.ukuz.piccolo.api.connection.ConnectionManager;
import io.github.ukuz.piccolo.api.exchange.handler.ChannelHandler;
import io.github.ukuz.piccolo.api.external.common.Assert;
import io.github.ukuz.piccolo.api.service.AbstractService;
import io.github.ukuz.piccolo.api.service.Service;
import io.github.ukuz.piccolo.api.service.ServiceException;
import io.github.ukuz.piccolo.api.spi.SpiLoader;
import io.github.ukuz.piccolo.common.properties.CoreProperties;
import io.github.ukuz.piccolo.transport.channel.SocketChannelFactory;
import io.github.ukuz.piccolo.transport.codec.Codec;
import io.github.ukuz.piccolo.transport.codec.DuplexCodec;
import io.github.ukuz.piccolo.transport.eventloop.EventLoopGroupFactory;
import io.github.ukuz.piccolo.transport.handler.ClientHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadFactory;

/**
 * @author ukuz90
 */
public abstract class NettyClient extends AbstractService implements Service {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private Bootstrap bootstrap;
    private EventLoopGroupFactory eventLoopGroupFactory;
    private ChannelFactory channelFactory;
    private ClientHandler handler;
    private EventLoopGroup workerGroup;
    protected Environment environment;
    protected ConnectionManager cxnxManager;

    public NettyClient(Environment environment, ConnectionManager cxnxManager, ChannelHandler handler) {
        Assert.notNull(environment, "environment must not be null");
        Assert.notNull(cxnxManager, "cxnxManager must not be null");
        Assert.notNull(handler, "handler must not be null");
        this.environment = environment;
        this.cxnxManager = cxnxManager;
        CoreProperties core = environment.getProperties(CoreProperties.class);
        if (core.isUseNettyEpoll()) {
            this.eventLoopGroupFactory = SpiLoader.getLoader(EventLoopGroupFactory.class).getExtension("epoll");
            this.channelFactory = SpiLoader.getLoader(SocketChannelFactory.class).getExtension("epoll");
        } else {
            this.eventLoopGroupFactory = SpiLoader.getLoader(EventLoopGroupFactory.class).getExtension("nio");
            this.channelFactory = SpiLoader.getLoader(SocketChannelFactory.class).getExtension("nio");
        }
        this.handler = new ClientHandler(environment, cxnxManager, handler);
    }

    @Override
    protected CompletableFuture<Boolean> doStartAsync() {
        logger.info("client start async...");
        CompletableFuture result = new CompletableFuture();
        bootstrap = new Bootstrap();
        workerGroup = eventLoopGroupFactory.newEventLoopGroup(1, 50, newWorkerThreadFactory());
        bootstrap.group(workerGroup);
        bootstrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        bootstrap.option(ChannelOption.SO_REUSEADDR, true);
        bootstrap.handler(new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel ch) throws Exception {
                initPipeline(ch.pipeline());
            }
        });
        bootstrap.channelFactory(channelFactory);
        initOptions(bootstrap);

        ChannelFuture channelFuture = bootstrap.connect(getInetSocketAddress());
        channelFuture.addListener(future -> {
            if (future.isSuccess()) {
                logger.info("client start async success: {}", channelFuture.channel().localAddress().toString().replace("/", ""));
                result.complete(true);
            } else {
                logger.error("client start async failure: {}", future.cause().getMessage());
                result.completeExceptionally(new ServiceException(future.cause()));
            }
        });

        return result;
    }

    protected void initPipeline(ChannelPipeline pipeline) {
        DuplexCodec codec = new DuplexCodec(cxnxManager, newCodec());
        pipeline.addLast("decoder", codec.getDecoder());
        pipeline.addLast("encoder", codec.getEncoder());
        pipeline.addLast("handler", handler);

    }

    protected void initOptions(Bootstrap bootstrap) {
        bootstrap.option(ChannelOption.TCP_NODELAY, true);
        bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 4000);
    }

    @Override
    public void destroy() throws ServiceException {
        workerGroup.shutdownGracefully();
    }

    protected ThreadFactory newWorkerThreadFactory() {
        return new DefaultThreadFactory(getWorkerThreadName());
    }

    protected abstract InetSocketAddress getInetSocketAddress();

    protected abstract String getWorkerThreadName();

    protected abstract Codec newCodec();

}
