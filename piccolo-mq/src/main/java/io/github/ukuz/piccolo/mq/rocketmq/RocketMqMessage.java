/*
 * Copyright 2021 ukuz90
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
package io.github.ukuz.piccolo.mq.rocketmq;

import io.github.ukuz.piccolo.api.external.common.Assert;
import io.github.ukuz.piccolo.mq.CommittableMqMessage;

import java.util.concurrent.CompletableFuture;

/**
 * @author ukuz90
 */
public abstract class RocketMqMessage extends CommittableMqMessage {

    private CompletableFuture<Boolean> future;

    public CompletableFuture<Boolean> getFuture() {
        return future;
    }

    public void setFuture(CompletableFuture<Boolean> future) {
        this.future = future;
    }

    @Override
    public void completeConsume() {
        Assert.notNull(mqClient, "mqClient must not be null");
        mqClient.commitMessage(this);
    }
}

