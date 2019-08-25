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
package io.github.ukuz.piccolo.server.boot;

import io.github.ukuz.piccolo.transport.server.NettyServer;

import java.util.concurrent.CompletableFuture;

/**
 * @author ukuz90
 */
public class ServerBoot implements BootJob {
    private NettyServer server;
    private boolean sync;

    public ServerBoot(NettyServer server) {
        this(server, false);
    }

    public ServerBoot(NettyServer server, boolean sync) {
        this.server = server;
        this.sync = sync;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void start() {
        if (sync) {
            if (server.start() && server.getRegistration() != null) {
                server.getPiccoloContext().getServiceRegistry().registry(server.getRegistration());
            }
        } else {
            CompletableFuture<Boolean> future = server.startAsync();
            future.whenCompleteAsync((success, throwable) -> {
                if (success && server.getRegistration() != null) {
                    server.getPiccoloContext().getServiceRegistry().registry(server.getRegistration());
                }
            });
        }

    }

    @SuppressWarnings("unchecked")
    @Override
    public void stop() {
        if (sync) {
            if (server.stop() && server.getRegistration() != null) {
                server.getPiccoloContext().getServiceRegistry().deregistry(server.getRegistration());
            }
        } else {
            CompletableFuture<Boolean> future = server.stopAsync();
            future.whenCompleteAsync((success, throwable) -> {
                if (success && server.getRegistration() != null) {
                    server.getPiccoloContext().getServiceRegistry().deregistry(server.getRegistration());
                }
            });
        }

    }
}
