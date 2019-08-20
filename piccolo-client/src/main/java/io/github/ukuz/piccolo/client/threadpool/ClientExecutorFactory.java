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
package io.github.ukuz.piccolo.client.threadpool;

import io.github.ukuz.piccolo.api.config.Environment;
import io.github.ukuz.piccolo.common.properties.ThreadPoolProperties;
import io.github.ukuz.piccolo.common.thread.ThreadNames;
import io.github.ukuz.piccolo.common.threadpool.AbstractExecutorFactory;
import io.github.ukuz.piccolo.common.threadpool.ThreadPoolConfig;

import java.util.concurrent.Executor;

/**
 * @author ukuz90
 */
public class ClientExecutorFactory extends AbstractExecutorFactory {
    @Override
    public Executor create(String name, Environment environment) {
        ThreadPoolProperties tp = environment.getProperties(ThreadPoolProperties.class);
        ThreadPoolConfig config;
        switch (name) {
            case PUSH_CLIENT:
                config = ThreadPoolConfig.builder()
                        .name(ThreadNames.T_PUSH_CLIENT_TIMER)
                        .coreSize(tp.getPushClient().getCoreSize())
                        .maxSize(tp.getPushClient().getMaxSize())
                        .queueCapacity(tp.getPushClient().getQueueSize())
                        .keepAliveSeconds(tp.getPushClient().getKeepAliveSeconds())
                        .build();
                return createScheduledExecutor(config);
            default:
                return super.create(name, environment);
        }
    }
}
