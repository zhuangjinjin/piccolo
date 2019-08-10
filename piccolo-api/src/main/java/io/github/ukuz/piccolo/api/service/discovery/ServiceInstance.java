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
package io.github.ukuz.piccolo.api.service.discovery;

import java.util.Map;

/**
 * @author ukuz90
 */
public interface ServiceInstance {

    /**
     * service name
     *
     * @return
     */
    String getServiceId();

    /**
     * instance id
     *
     * @return
     */
    String getInstanceId();

    /**
     * service instance's host
     *
     * @return
     */
    String getHost();

    /**
     * service instance expose port
     *
     * @return
     */
    int getPort();

    /**
     * get metadata by key
     *
     * @param key
     * @return
     */
    default Map<String, String> getMetaData(String key) {
        return null;
    }

    /**
     * is serivce instance persistent
     *
     * @return
     */
    default boolean isPersistent() {
        return false;
    }

    /**
     * host and port
     *
     * @return
     */
    default String getHostAndPort() {
        return getHost() + ":" + getPort();
    }

    /**
     * service path
     *
     * @return
     */
    default String getServicePath() {
        return getServiceId() + "/" + getInstanceId();
    }

}
