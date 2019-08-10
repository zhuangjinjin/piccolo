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
package io.github.ukuz.piccolo.common.threadpool;

import io.github.ukuz.piccolo.common.thread.NamedThreadFactory;
import lombok.Builder;
import lombok.Getter;

import java.util.concurrent.*;

/**
 * @author ukuz90
 */
@Builder
@Getter
public final class ThreadPoolConfig {

    public static final int REJECTED_POLICY_ABORT = 0;
    public static final int REJECTED_POLICY_CALL_RUNNER = 1;
    public static final int REJECTED_POLICY_DISCARD = 2;
    public static final int REJECTED_POLICY_DISCARD_OLDEST = 3;

    private String name;
    private int coreSize;
    private int maxSize;
    private int keepAliveSeconds;
    private int queueCapacity;

    private int rejectedPolicy;

    BlockingQueue<Runnable> getTaskQueue() {
        if (queueCapacity == 0) {
            // not cache, will blocking
            return new SynchronousQueue<>();
        } else if (queueCapacity > 0) {
            return new LinkedBlockingQueue<>(queueCapacity);
        } else {
            return new LinkedBlockingQueue<>();
        }
    }

    ThreadFactory getThreadFactory() {
        return new NamedThreadFactory(name);
    }

    RejectedExecutionHandler getExecutionHandler() {
        switch (rejectedPolicy) {
            case REJECTED_POLICY_ABORT:
                return new ThreadPoolExecutor.AbortPolicy();
            case REJECTED_POLICY_CALL_RUNNER:
                return new ThreadPoolExecutor.CallerRunsPolicy();
            case REJECTED_POLICY_DISCARD:
                return new ThreadPoolExecutor.DiscardPolicy();
            case REJECTED_POLICY_DISCARD_OLDEST:
                return new ThreadPoolExecutor.DiscardOldestPolicy();
            default:
                 throw new IllegalArgumentException("invalid rejected policy, " + rejectedPolicy);
        }
    }


}
