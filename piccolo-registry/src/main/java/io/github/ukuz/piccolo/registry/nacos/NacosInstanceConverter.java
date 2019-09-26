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
package io.github.ukuz.piccolo.registry.nacos;

import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.naming.utils.NamingUtils;
import io.github.ukuz.piccolo.api.external.common.Assert;
import io.github.ukuz.piccolo.api.service.discovery.DefaultServiceInstance;

import java.util.Optional;

/**
 * @author ukuz90
 */
public final class NacosInstanceConverter {

    private NacosInstanceConverter() {}

    public static DefaultServiceInstance covert(Instance instance) {
        Assert.notNull(instance, "instance must not be null");

        final DefaultServiceInstance result = DefaultServiceInstance.build()
                .host(toOrigintring(instance.getIp()))
                .port(instance.getPort());

        Optional.ofNullable(toOrigintring(instance.getServiceName()))
                .map(NamingUtils::getServiceName)
                .ifPresent(result::setServiceId);
        return result;
    }

    public static Instance convert(DefaultServiceInstance instance) {
        Assert.notNull(instance, "instance must not be null");
        Instance result = new Instance();
        result.setIp(toNacosString(instance.getHost()));
        result.setPort(instance.getPort());
        result.setWeight(1.0);
        return result;
    }

    public static String toNacosString(String str) {
        if (str == null) {
            return null;
        }
        return str.replace('/', '-');
    }

    private static String toOrigintring(String str) {
        if (str == null) {
            return null;
        }
        return str.replace('-', '/');
    }

}
