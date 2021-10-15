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
package io.github.ukuz.piccolo.api;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author zhuangjj
 */
public class AsyncContext {

    private Map<String, Object> arguments = new HashMap<>();
    private static ThreadLocal<AsyncContext> current = new ThreadLocal<>();

    public static AsyncContext getContext() {
        AsyncContext asyncContext = current.get();
        if (asyncContext == null) {
            asyncContext = new AsyncContext();
            current.set(asyncContext);
        }
        return asyncContext;
    }

    public void restoreContext(AsyncContext asyncContext) {
        current.set(asyncContext);
    }

    public Object getArgument(String key) {
        return arguments.get(key);
    }

    public void setArgument(String key, Object val) {
        arguments.put(key, val);
    }

    public void destroy() {
        AsyncContext asyncContext = current.get();
        if (Objects.nonNull(asyncContext)) {
            asyncContext.arguments.clear();
        }
        current.remove();
    }

}
