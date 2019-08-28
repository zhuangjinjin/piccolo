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
package io.github.ukuz.piccolo.api.common.remote;

import io.github.ukuz.piccolo.api.id.IdGenException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ukuz90
 */
public class FailoverInvoker implements Invoker {

    private static final Logger LOGGER = LoggerFactory.getLogger(FailoverInvoker.class);

    @Override
    public Object invoke(InvocationHandler invocation) throws Exception {
        int times = Integer.getInteger("piccolo.failover.times", 3);
        for (int i = 0; i < times; i++) {
            try {
                return invocation.process();
            } catch (Exception e) {
                if (isBizException(e)) {
                    throw e;
                }
                LOGGER.error("invoke failure {} times, cause: {}", i, e);
            }
        }
        return null;
    }

    private boolean isBizException(Exception e) {
        return e instanceof IdGenException;
    }
}
