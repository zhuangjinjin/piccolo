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

import io.github.ukuz.piccolo.api.common.Assert;
import io.github.ukuz.piccolo.api.exchange.handler.ChannelHandler;
import io.github.ukuz.piccolo.api.service.AbstractService;
import io.github.ukuz.piccolo.api.service.IllegalStateServiceException;
import io.github.ukuz.piccolo.api.service.Server;
import io.github.ukuz.piccolo.api.service.ServiceException;
import io.github.ukuz.piccolo.transport.eventloop.EventLoopGroupFactory;
import io.github.ukuz.piccolo.transport.handler.ServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.util.concurrent.DefaultThreadFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author ukuz90
 */
public abstract class NettyServer extends AbstractService implements Server {

    private ServerBootstrap server;
    private EventLoopGroupFactory eventLoopGroupFactory;
    private ServerHandler serverHandler;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    public NettyServer(EventLoopGroupFactory eventLoopGroupFactory, ChannelHandler channelHandler) {
        Assert.notNull(eventLoopGroupFactory, "eventLoopGroupFactory must not be null");
        Assert.notNull(channelHandler, "channelHandler must not be null");
        this.eventLoopGroupFactory = eventLoopGroupFactory;
        this.serverHandler = new ServerHandler(channelHandler);
    }

    private final AtomicReference<State> serverState = new AtomicReference<>(State.Created);

    @Override
    public final void init() throws ServiceException {
        if (!serverState.compareAndSet(State.Created, State.Initializing)) {
            throw new IllegalStateServiceException("Server " + getId() + " init failed, current state: " + serverState.get());
        }

        doInit();
        serverState.set(State.Initialized);
    }

    @Override
    protected CompletableFuture doStartAsync() {
        if (!serverState.compareAndSet(State.Initialized, State.Starting)) {
            throw new IllegalStateServiceException("Server " + getId() + " start failed, current state: " + serverState.get());
        }

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

        server.group(bossGroup, workerGroup);

        initOptions(server);

        server.childHandler(new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel ch) throws Exception {
                initPipeline(ch.pipeline());
            }
        });

        ChannelFuture channelFuture = server.bind(getInetSocketAddress());
        channelFuture.addListener( future ->  {
            if (future.isSuccess()) {
                serverState.set(State.Started);
                result.complete(true);
            } else {
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
        doDestory();

        serverState.set(State.Terminated);
    }

    protected void initOptions(ServerBootstrap server) {
        server.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        server.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
    }

    protected void initPipeline(ChannelPipeline pipeline) {
        pipeline.addLast("decoder", getDecoder());
        pipeline.addLast("encoder", getEncoder());
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

    protected abstract ChannelOutboundHandler getEncoder();

    protected abstract ChannelInboundHandler getDecoder();

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
