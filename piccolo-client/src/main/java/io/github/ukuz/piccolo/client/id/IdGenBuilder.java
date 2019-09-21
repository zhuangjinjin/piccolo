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
import io.github.ukuz.piccolo.api.id.IdGenException;
import io.github.ukuz.piccolo.client.PiccoloClient;

/**
 * @author ukuz90
 */
public class IdGenBuilder {

    private IdGen idGen;

    private IdGenBuilder() {
        idGen = PiccoloClient.getInstance().getIdGen();
    }

    public static IdGenBuilder build() {
        return new IdGenBuilder();
    }

    /**
     * generate an xid in distributed system
     * @return
     * @throws IdGenException
     */
    public long genXid() throws IdGenException {
        return idGen.get(null);
    }

}
