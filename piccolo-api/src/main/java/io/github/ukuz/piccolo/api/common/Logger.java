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
package io.github.ukuz.piccolo.api.common;

import org.slf4j.LoggerFactory;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author ukuz90
 */
public class Logger {

    private final org.slf4j.Logger logger;

    private Logger(Class<?> clazz) {
        logger = LoggerFactory.getLogger(clazz);
    }

    public static Logger getLogger(Class<?> clazz) {
        return new Logger(clazz);
    }

    public void trace(String msg, Supplier<Object[]> arguments) {
        if (logger.isTraceEnabled()) {
            logger.trace(msg, arguments.get());
        }
    }

    public void debug(String msg, Supplier<Object[]> arguments) {
        if (logger.isDebugEnabled()) {
            logger.debug(msg, arguments.get());
        }
    }

    public void info(String msg, Supplier<Object[]> arguments) {
        if (logger.isInfoEnabled()) {
            logger.info(msg, arguments.get());
        }
    }

    public void warn(String msg, Supplier<Object[]> arguments) {
        if (logger.isWarnEnabled()) {
            logger.warn(msg, arguments.get());
        }
    }

    public void error(String msg, Supplier<Object[]> arguments) {
        if (logger.isErrorEnabled()) {
            logger.error(msg, arguments.get());
        }
    }

    public void computeIfTraceEnable(Consumer<org.slf4j.Logger> consumer) {
        if (logger.isTraceEnabled()) {
            consumer.accept(logger);
        }
    }

    public void computeIfDebugEnable(Consumer<org.slf4j.Logger> consumer) {
        if (logger.isDebugEnabled()) {
            consumer.accept(logger);
        }
    }

    public void computeIfInfoEnable(Consumer<org.slf4j.Logger> consumer) {
        if (logger.isInfoEnabled()) {
            consumer.accept(logger);
        }
    }

    public void computeIfWarnEnable(Consumer<org.slf4j.Logger> consumer) {
        if (logger.isWarnEnabled()) {
            consumer.accept(logger);
        }
    }

    public void computeIfErrorEnable(Consumer<org.slf4j.Logger> consumer) {
        if (logger.isErrorEnabled()) {
            consumer.accept(logger);
        }
    }
}
