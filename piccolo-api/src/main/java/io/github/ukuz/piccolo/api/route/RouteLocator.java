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
package io.github.ukuz.piccolo.api.route;

import io.github.ukuz.piccolo.api.service.Service;
import io.github.ukuz.piccolo.api.spi.Spi;

/**
 * @author ukuz90
 */
@Spi(primary = "cache")
public interface RouteLocator<T, R> extends Service {

    /**
     * add route
     * @param routeKey
     * @param service
     */
    void route(T routeKey, R service);

    /**
     * get route
     * @param routeKey
     * @return
     */
    R getRoute(T routeKey);
}