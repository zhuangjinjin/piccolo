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

import io.github.ukuz.piccolo.api.service.AbstractService;
import io.github.ukuz.piccolo.api.service.Service;
import io.github.ukuz.piccolo.api.service.ServiceException;
import io.github.ukuz.piccolo.transport.eventloop.EventLoopGroupFactory;
import io.github.ukuz.piccolo.transport.handler.ClientHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.util.concurrent.DefaultThreadFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadFactory;

/**
 * @author ukuz90
 */
public abstract class NettyClient extends AbstractService implements Service {

    private Bootstrap bootstrap;
    private EventLoopGroupFactory eventLoopGroupFactory;
    private ClientHandler handler;
    private EventLoopGroup workerGroup;

    public NettyClient(EventLoopGroupFactory eventLoopGroupFactory, ClientHandler handler) {
        this.eventLoopGroupFactory = eventLoopGroupFactory;
        this.handler = handler;
    }

    @Override
    protected CompletableFuture<Boolean> doStartAsync() {
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
        initOptions(bootstrap);

        ChannelFuture channelFuture = bootstrap.connect(getInetSocketAddress());
        channelFuture.addListener(future -> {
            if (future.isSuccess()) {
                result.complete(true);
            } else {
                result.completeExceptionally(new ServiceException(future.cause()));
            }
        });

        return result;
    }

    protected void initPipeline(ChannelPipeline pipeline) {
        pipeline.addLast("decoder", getDecoder());
        pipeline.addLast("encoder", getEncoder());
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

    protected abstract ChannelOutboundHandler getEncoder();

    protected abstract ChannelInboundHandler getDecoder();

}
