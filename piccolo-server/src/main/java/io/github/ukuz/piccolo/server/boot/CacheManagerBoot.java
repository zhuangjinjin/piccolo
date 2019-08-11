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
package io.github.ukuz.piccolo.server.boot;

import io.github.ukuz.piccolo.api.PiccoloContext;
import io.github.ukuz.piccolo.api.cache.CacheManager;
import io.github.ukuz.piccolo.server.boot.BootJob;

/**
 * @author ukuz90
 */
public class CacheManagerBoot implements BootJob {

    private CacheManager cacheManager;
    private PiccoloContext piccoloContext;

    public CacheManagerBoot(CacheManager cacheManager, PiccoloContext piccoloContext) {
        this.cacheManager = cacheManager;
        this.piccoloContext = piccoloContext;
    }

    @Override
    public void start() {
        this.cacheManager.init(piccoloContext);
    }

    @Override
    public void stop() {
        this.cacheManager.destroy();
    }
}
