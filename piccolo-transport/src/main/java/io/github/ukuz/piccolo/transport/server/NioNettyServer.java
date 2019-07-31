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

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

import java.net.InetSocketAddress;

/**
 * @author ukuz90
 */
public abstract class NioNettyServer extends NettyServer {

    private String id;
    private InetSocketAddress address;

    public NioNettyServer(String id, InetSocketAddress address) {
        this.id = id;
        this.address = address;
    }

    @Override
    protected EventLoopGroup getBossThreadPool() {
        NioEventLoopGroup bossGroup = new NioEventLoopGroup(getBossThreadNum(), getBossThreadFactory());
        bossGroup.setIoRatio(getBossIORatio());
        return bossGroup;
    }

    @Override
    protected EventLoopGroup getWorkerThreadPool() {
        NioEventLoopGroup workerGroup = new NioEventLoopGroup(getWorkerThreadNum(), getWorkerThreadFactory());
        workerGroup.setIoRatio(getWorkerIORatio());
        return workerGroup;
    }

    @Override
    protected InetSocketAddress getInetSocketAddress() {
        return address;
    }

    @Override
    public String getId() {
        return id;
    }

}
