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
package io.github.ukuz.piccolo.api.router;

import java.util.Set;

/**
 * @author ukuz90
 */
public interface RouterManager<R extends Router> {

    /**
     * register new router by userId
     *
     * @param userId userId
     * @param router new Router
     * @return previous Router
     */
    R register(String userId, R router);

    /**
     * unregister router by userId and clientType
     *
     * @param userId
     * @param clientType
     * @return
     */
    boolean unregister(String userId, byte clientType);

    /**
     * lookup router by userId and clientType
     *
     * @param userId
     * @param clientType
     * @return
     */
    R lookup(String userId, byte clientType);

    /**
     * lookup all router by userId
     * @param userId
     * @return
     */
    Set<R> lookupAll(String userId);

}
