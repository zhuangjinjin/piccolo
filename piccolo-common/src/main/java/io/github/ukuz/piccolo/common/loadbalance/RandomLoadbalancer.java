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
package io.github.ukuz.piccolo.common.loadbalance;

import io.github.ukuz.piccolo.api.loadbalance.AbstractLoadbalancer;
import io.github.ukuz.piccolo.api.loadbalance.LoadBalancer;
import io.github.ukuz.piccolo.api.service.discovery.ServiceInstance;

import java.util.List;
import java.util.Random;

/**
 * @author ukuz90
 */
public class RandomLoadbalancer extends AbstractLoadbalancer implements LoadBalancer {
    @Override
    protected ServiceInstance doChoose(List<? extends ServiceInstance> serviceInstances) {
        Random random = new Random();
        int index = random.nextInt(serviceInstances.size());
        return serviceInstances.get(index);
    }
}
