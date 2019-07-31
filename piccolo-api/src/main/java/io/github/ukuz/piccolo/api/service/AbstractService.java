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

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author ukuz90
 */
public abstract class AbstractService implements Service {

    private final AtomicBoolean isStarted = new AtomicBoolean();

    @Override
    public CompletableFuture<Boolean> startAsync() {
        return startAsync(null);
    }

    @Override
    public CompletableFuture<Boolean> stopAsync() {
        return stopAsync(null);
    }

    @Override
    public final CompletableFuture<Boolean> startAsync(Callback callback) throws ServiceException {
        ServiceCallback serviceCallback = wrapServiceCallback(callback);
        if  (isStarted.compareAndSet(false, true)) {
            try {
                init();
                CompletableFuture future = doStartAsync();
                future.whenCompleteAsync((result, throwable) -> {
                    Optional.of(result).ifPresent(r-> serviceCallback.success());
                    Optional.of(throwable).ifPresent(t->serviceCallback.failure(wrapServiceException((Throwable) t)));
                });
            } catch (ServiceException e) {
                serviceCallback.failure(e);
            }
        } else {
            //duplicate start
            serviceCallback.failure(new DuplicateStartServiceException("Service " + this.getClass().getName() + " already started."));
        }
        return serviceCallback;
    }

    @Override
    public final CompletableFuture<Boolean> stopAsync(Callback callback) {
        ServiceCallback serviceCallback = wrapServiceCallback(callback);
        if (!isStarted.get()) {
            serviceCallback.failure(new IllegalStateServiceException("Service " + this.getClass().getName() + " was not running."));
            return serviceCallback;
        }
        if (isStarted.compareAndSet(true, false)) {
            try {
                destory();
                serviceCallback.success();
            } catch (ServiceException e) {
                serviceCallback.failure(e);
            }
        } else {
            //duplicate stop
            serviceCallback.failure(new DuplicateStopServiceException("Service " + this.getClass().getName() + " already stopped."));
        }
        return serviceCallback;
    }

    @Override
    public final boolean start() throws ServiceException {
        try {
            return startAsync(null).join();
        } catch (CompletionException e) {
            throw wrapServiceException(e.getCause());
        }
    }

    @Override
    public final boolean stop() throws ServiceException {
        try {
            return stopAsync(null).join();
        } catch (CompletionException e) {
            throw wrapServiceException(e.getCause());
        }

    }

    protected CompletableFuture<Boolean> doStartAsync() {
        CompletableFuture future = new CompletableFuture<>();
        future.complete(true);
        return future;
    }

    private ServiceCallback wrapServiceCallback(Callback callback) {
        if (callback instanceof ServiceCallback) {
            return (ServiceCallback) callback;
        }
        return new ServiceCallback(isStarted, callback);
    }

    protected ServiceException wrapServiceException(Throwable throwable) {
        if (throwable instanceof ServiceException) {
            return (ServiceException) throwable;
        }
        return new ServiceException(throwable);
    }

    @Override
    public boolean isRunning() {
        throw new UnsupportedOperationException();
    }
}
