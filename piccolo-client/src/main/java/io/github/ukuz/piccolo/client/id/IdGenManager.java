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
package io.github.ukuz.piccolo.client.id;

import io.github.ukuz.piccolo.api.id.IdGen;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author ukuz90
 */
public class IdGenManager {

    public static final IdGenManager instance = new IdGenManager();

    private final ConcurrentHashMap<Long, UniqueIdGen> pools = new ConcurrentHashMap<>();

    private final AtomicLong id = new AtomicLong(0);

    public void register(UniqueIdGen idGen) {
        pools.put(idGen.getId(), idGen);
    }

    public void unregister(UniqueIdGen idGen) {
        pools.remove(idGen.getId());
    }

    public Long acquireId() {
        return id.incrementAndGet();
    }

    public static IdGenManager getInstance() {
        return instance;
    }

    public IdGen get(Long id) {
        if (pools.containsKey(id)) {
            return pools.get(id);
        } else {
            throw new IllegalArgumentException("Have not found id:" + id + " idGen");
        }
    }

}
