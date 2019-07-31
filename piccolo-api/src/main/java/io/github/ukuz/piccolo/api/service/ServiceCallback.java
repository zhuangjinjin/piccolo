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
package io.github.ukuz.piccolo.api.service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author ukuz90
 */
public class ServiceCallback extends CompletableFuture<Boolean> implements Callback {

    private AtomicBoolean result;
    private Callback callback;

    public ServiceCallback(AtomicBoolean result) {
        this.result = result;
    }

    public ServiceCallback(AtomicBoolean result, Callback callback) {
        this.result = result;
        this.callback = callback;
    }

    @Override
    public void success(Object... args) {
        if (this.isDone()) {
            return;
        }

        this.complete(result.get());
        if (callback != null) {
            callback.success(args);
        }
    }

    @Override
    public void failure(Throwable throwable, Object... args) {
        if (this.isDone()) {
            return;
        }
        this.completeExceptionally(throwable);

        if (callback != null) {
            callback.failure(throwable, args);
        }
    }
}
