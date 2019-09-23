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
package io.github.ukuz.piccolo.client.route;

import io.github.ukuz.piccolo.api.external.common.Assert;
import io.github.ukuz.piccolo.api.route.RouteLocator;
import io.github.ukuz.piccolo.client.PiccoloClient;

/**
 * @author ukuz90
 */
public final class RouteLocatorBuilder {

    private RouteLocator routeLocator;

    private RouteLocatorBuilder() {
        this.routeLocator = PiccoloClient.getInstance().getRouteLocator();
    }

    public static RouteLocatorBuilder routes() {
        return new RouteLocatorBuilder();
    }

    public RouteLocatorBuilder route(String routeKey, String service) {
        Assert.notEmptyString(routeKey, "routeKey must not be empty");
        Assert.notEmptyString(service, "service must not be empty");
        routeLocator.route(routeKey, service);
        return this;
    }

}
