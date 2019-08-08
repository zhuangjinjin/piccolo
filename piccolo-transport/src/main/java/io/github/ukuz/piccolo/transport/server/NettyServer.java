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
package io.github.ukuz.piccolo.transport.server;

import io.github.ukuz.piccolo.api.PiccoloContext;
import io.github.ukuz.piccolo.api.config.Environment;
import io.github.ukuz.piccolo.api.external.common.Assert;
import io.github.ukuz.piccolo.api.connection.ConnectionManager;
import io.github.ukuz.piccolo.api.exchange.handler.ChannelHandler;
import io.github.ukuz.piccolo.api.service.AbstractService;
import io.github.ukuz.piccolo.api.service.IllegalStateServiceException;
import io.github.ukuz.piccolo.api.service.Server;
import io.github.ukuz.piccolo.api.service.ServiceException;
import io.github.ukuz.piccolo.api.spi.SpiLoader;
import io.github.ukuz.piccolo.common.properties.CoreProperties;
import io.github.ukuz.piccolo.transport.channel.ServerSocketChannelFactory;
import io.github.ukuz.piccolo.transport.codec.Codec;
import io.github.ukuz.piccolo.transport.codec.DuplexCodec;
import io.github.ukuz.piccolo.transport.eventloop.EventLoopGroupFactory;
import io.github.ukuz.piccolo.transport.handler.ServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author ukuz90
 */
public abstract class NettyServer extends AbstractService implements Server {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private ServerBootstrap server;
    private EventLoopGroupFactory eventLoopGroupFactory;
    private ChannelFactory<ServerSocketChannel> channelFactory;
    private ServerHandler serverHandler;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private PiccoloContext piccoloContext;

    private final AtomicReference<State> serverState = new AtomicReference<>(State.Created);

    public NettyServer(PiccoloContext piccoloContext, ChannelHandler channelHandler, ConnectionManager cxnxManager) {
        Assert.notNull(piccoloContext, "piccoloContext must not be null");
        Assert.notNull(channelHandler, "channelHandler must not be null");
        Assert.notNull(channelHandler, "cxnxManager must not be null");
        this.piccoloContext = piccoloContext;
        Environment environment = piccoloContext.getEvironment();
        CoreProperties core = environment.getProperties(CoreProperties.class);
        if (core.isUseNettyEpoll()) {
            this.eventLoopGroupFactory = SpiLoader.getLoader(EventLoopGroupFactory.class).getExtension("epoll");
            this.channelFactory = SpiLoader.getLoader(ServerSocketChannelFactory.class).getExtension("epoll");
        } else {
            this.eventLoopGroupFactory = SpiLoader.getLoader(EventLoopGroupFactory.class).getExtension("nio");
            this.channelFactory = SpiLoader.getLoader(ServerSocketChannelFactory.class).getExtension("nio");
        }
        this.serverHandler = new ServerHandler(cxnxManager, channelHandler);
    }

    @Override
    public final void init() throws ServiceException {
        if (!serverState.compareAndSet(State.Created, State.Initializing)) {
            throw new IllegalStateServiceException("Server " + getId() + " init failed, current state: " + serverState.get());
        }

        logger.info("server start init...");
        doInit();
        serverState.set(State.Initialized);
        logger.info("server finish init...");
    }

    @Override
    protected CompletableFuture doStartAsync() {
        if (!serverState.compareAndSet(State.Initialized, State.Starting)) {
            throw new IllegalStateServiceException("Server " + getId() + " start failed, current state: " + serverState.get());
        }

        logger.info("server start async...");
        CompletableFuture result = new CompletableFuture();
        server = new ServerBootstrap();
        bossGroup = createBossThreadPool();
        workerGroup = createWorkerThreadPool();

        if (bossGroup == null) {
            bossGroup = NettyServer.this.createBossThreadPool();
        }

        if (workerGroup == null) {
            workerGroup = NettyServer.this.createWorkerThreadPool();
        }

        server.channelFactory(channelFactory);
        server.group(bossGroup, workerGroup);

        initOptions(server);

        server.childHandler(new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel ch) {
                initPipeline(ch.pipeline());
            }
        });

        ChannelFuture channelFuture = server.bind(getInetSocketAddress());
        channelFuture.addListener( future ->  {
            if (future.isSuccess()) {

                serverState.set(State.Started);
                logger.info("server start async success: {}", channelFuture.channel().localAddress().toString().replace("/",""));
                result.complete(true);
            } else {
                logger.error("server start failure: {}", future.cause().getMessage());
                result.completeExceptionally(new ServiceException(future.cause()));
            }
        });

        return result;
    }

    @Override
    public final void destroy() throws ServiceException {
        if (!serverState.compareAndSet(State.Started, State.Shutdown)) {
            throw new IllegalStateServiceException("Server " + getId() + " destroy failed, current state: " + serverState.get());
        }
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
        doDestory();

        serverState.set(State.Terminated);
    }

    protected void initOptions(ServerBootstrap server) {
        server.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        server.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
    }

    protected void initPipeline(ChannelPipeline pipeline) {
        DuplexCodec codec = new DuplexCodec(newCodec());
        pipeline.addLast("decoder", codec.getDecoder());
        pipeline.addLast("encoder", codec.getEncoder());
        pipeline.addLast("handler", serverHandler);
    }

    private EventLoopGroup createBossThreadPool() {
        return eventLoopGroupFactory.newEventLoopGroup(getBossThreadNum(),
                getBossIORatio(), newBossThreadFactory());
    }

    private EventLoopGroup createWorkerThreadPool() {
        return eventLoopGroupFactory.newEventLoopGroup(getWorkerThreadNum(),
                getWorkerIORatio(), newWorkerThreadFactory());
    }

    @Override
    public boolean isRunning() {
        return serverState.get() == State.Started;
    }

    protected ThreadFactory newBossThreadFactory() {
        return new DefaultThreadFactory(getBossThreadName());
    }

    protected ThreadFactory newWorkerThreadFactory() {
        return new DefaultThreadFactory(getWorkerThreadName());
    }

//    protected abstract ChannelOutboundHandler getEncoder();
//
//    protected abstract ChannelInboundHandler getDecoder();

    protected abstract Codec newCodec();

    protected abstract void doInit();

    protected abstract void doDestory();

    protected abstract InetSocketAddress getInetSocketAddress();

    public int getBossIORatio() {
        return 100;
    }

    public abstract int getWorkerIORatio();

    public abstract int getBossThreadNum();

    public abstract int getWorkerThreadNum();

    public abstract String getBossThreadName();

    public abstract String getWorkerThreadName();

    public enum State {
        Created,
        Initializing,
        Initialized,
        Starting,
        Started,
        Shutdown,
        Terminated,
    }

}
