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

import io.github.ukuz.piccolo.api.annotation.Order;

import java.util.List;

/**
 * @author ukuz90
 */
public class DefaultOrderComparator<T extends Class> implements OrderComparator<T> {

    @Override
    public List<T> compare(List<T> list) {
        return compare(list, true);
    }

    @Override
    public List<T> compare(List<T> list, final boolean asc) {
        list.sort((o1, o2) -> {
            Order order1 = getOrderAnnotation(o1);
            Order order2 = getOrderAnnotation(o2);
            return asc ?
                    order1.value() - order2.value() :
                    order2.value() - order1.value();
        });
        return list;
    }

    private Order getOrderAnnotation(Class<T> t) {
        Order order = (Order) t.getAnnotation(Order.class);
        if (order == null) {
            throw new IllegalArgumentException("Class " + t.getName() + " doesn't annotated with @Order");
        }
        return order;
    }
}
