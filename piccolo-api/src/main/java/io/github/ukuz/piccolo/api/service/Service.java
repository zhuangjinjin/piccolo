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

import io.github.ukuz.piccolo.api.PiccoloContext;

import java.util.concurrent.CompletableFuture;

/**
 * @author ukuz90
 * @see AbstractService
 * @see Callback
 * @see ServiceCallback
 */
public interface Service {

    /**
     * asynchronous start service.
     *
     * @return
     */
    CompletableFuture<Boolean> startAsync();

    /**
     * asynchronous stop service.
     *
     * @return
     */
    CompletableFuture<Boolean> stopAsync();

    /**
     * asynchronus start service
     * @param context
     * @return
     */
    CompletableFuture<Boolean> startAsync(PiccoloContext context);

    /**
     * synchronous start service.
     *
     * @return
     * @throws ServiceException
     */
    boolean start() throws ServiceException;

    /**
     * synchronous stop service.
     *
     * @return
     * @throws ServiceException
     */
    boolean stop() throws ServiceException;

    /**
     * synchronous init service.
     *
     * @throws ServiceException
     */
    default void init() throws ServiceException {}

    /**
     * synchronous init service.
     * @param context
     * @throws ServiceException
     */
    default void init(PiccoloContext context) throws ServiceException {}

    /**
     * synchronous destroy service.
     *
     * @throws ServiceException
     */
    default void destroy() throws ServiceException {}

    /**
     * is service running.
     * 
     * @return
     */
    boolean isRunning();

}
