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
package io.github.ukuz.piccolo.common.event;

import com.google.common.eventbus.AsyncEventBus;
import io.github.ukuz.piccolo.api.event.ApplicationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executor;

/**
 * @author ukuz90
 */
public final class EventBus {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventBus.class);

    private static com.google.common.eventbus.EventBus delegate;

    public static void create(Executor executor) {
        delegate = new AsyncEventBus(executor, ((exception, context) -> {
            exception.printStackTrace();
            LOGGER.error("event bus subscriber failure, cause: {} context: {}", exception, context.toString());
        }));
    }

    public static void post(ApplicationEvent event) throws EventException {
        if (delegate == null) {
            throw new EventException("eventBus was not initialized");
        }
        delegate.post(event);
    }

    public static void register(Object obj) {
        if (delegate == null) {
            throw new EventException("eventBus was not initialized");
        }
        delegate.register(obj);
    }

    public static void unregister(Object obj) {
        if (delegate == null) {
            throw new EventException("eventBus was not initialized");
        }
        delegate.unregister(obj);
    }

}
