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

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.ResourceLock;

import java.io.FileNotFoundException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.parallel.ResourceAccessMode.*;

class AbstractServiceTest {

    private AbstractService service;
    private static AbstractService globalService;

    @BeforeEach
    void setUp() {
        service = new AbstractService() {
            @Override
            public void init() throws ServiceException {
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    throw new ServiceException(e);
                }
            }
        };
    }

    @BeforeAll
    static void setUpAll() {
        globalService = new AbstractService() {
            @Override
            public void init() throws ServiceException {
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    throw new ServiceException(e);
                }
            }
        };
    }

    @DisplayName("test_startAsync_WithException")
    @Test
    void testStartAsyncWithException() {
        AbstractService serviceStartWithException = new AbstractService() {
            @Override
            public void init() throws ServiceException {
                throw new ServiceException(new IllegalArgumentException("Config File was invalid."));
            }
        };
        CompletableFuture<Boolean> future = serviceStartWithException.startAsync();
        try {
            future.join();
            fail();
        } catch (CompletionException e) {
            assertEquals(ServiceException.class, e.getCause().getClass());
            assertEquals("io.github.ukuz.piccolo.api.service.ServiceException: java.lang.IllegalArgumentException: Config File was invalid.", e.getMessage());
        }
    }

    @DisplayName("test_startAsync")
    @Test
    void testStartAsync() {
        CompletableFuture<Boolean> future = service.startAsync(new EmptyCallback());
        assertTrue(future.join());
    }

    @DisplayName("test_stopAsync_WithException")
    @Test
    void testStopAsyncWithException() {
        AbstractService serviceStopWithException = new AbstractService() {
            @Override
            public void destory() throws ServiceException {
                throw new ServiceException(new FileNotFoundException("Resource was not exist."));
            }
        };
        serviceStopWithException.start();
        CompletableFuture<Boolean> future = serviceStopWithException.stopAsync();
        try {
            future.join();
            fail();
        } catch (CompletionException e) {
            assertEquals(ServiceException.class, e.getCause().getClass());
            assertEquals("io.github.ukuz.piccolo.api.service.ServiceException: java.io.FileNotFoundException: Resource was not exist.", e.getMessage());
        }
    }

    @DisplayName("test_stopAsync")
    @Test
    void testStopAsync() {
        CompletableFuture<Boolean> startFuture = service.startAsync(new EmptyCallback());
        assertTrue(startFuture.join());
        CompletableFuture<Boolean> stopFuture = service.stopAsync(new EmptyCallback());
        assertFalse(stopFuture.join());
    }

    @DisplayName("test_start")
    @Test
    void testStart() {
        assertTrue(service.start());
    }

    @DisplayName("test_stop_WithException")
    @Test
    void stopWithException() {
        AbstractService serviceStopWithException = new AbstractService() {
            @Override
            public void destory() throws ServiceException {
                throw new ServiceException(new FileNotFoundException("Resource was not exist."));
            }
        };
        serviceStopWithException.start();
        try {
            serviceStopWithException.stop();
            fail();
        } catch (ServiceException e) {
            assertEquals(FileNotFoundException.class, e.getCause().getClass());
            assertEquals("java.io.FileNotFoundException: Resource was not exist.", e.getMessage());
        }
    }

    @DisplayName("test_stop")
    @Test
    void stop() {
        assertTrue(service.start());
        assertFalse(service.stop());
    }

    @Execution(value = ExecutionMode.CONCURRENT)
    @DisplayName("test_start_WithConcurrent")
    @Test
    @ResourceLock(value = "duplicateStart", mode = READ_WRITE)
    void startWithConcurrent() {
        assertTrue(globalService.start());
    }

    @Execution(value = ExecutionMode.CONCURRENT)
    @DisplayName("test_start_WithConcurrentDuplicate")
    @Test
    @ResourceLock(value = "duplicateStart", mode = READ)
    void startWithConcurrentDuplicate() {
        try {
            globalService.start();
            fail();
        } catch (ServiceException e) {
            assertEquals(DuplicateStartServiceException.class, e.getClass());
        }
    }

    private static class EmptyCallback implements Callback {

        @Override
        public void success(Object... args) {

        }

        @Override
        public void failure(Throwable throwable, Object... args) {

        }
    }
}