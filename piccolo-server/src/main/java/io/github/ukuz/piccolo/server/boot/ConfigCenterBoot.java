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
import io.github.ukuz.piccolo.api.configcenter.DynamicConfiguration;

/**
 * @author ukuz90
 */
public class ConfigCenterBoot implements BootJob {

    private DynamicConfiguration configCenter;
    private PiccoloContext context;

    public ConfigCenterBoot(DynamicConfiguration configCenter, PiccoloContext context) {
        this.configCenter = configCenter;
        this.context = context;
    }

    @Override
    public void start() {
        //必须同步调用。
        this.configCenter.startAsync(context).join();
    }

    @Override
    public void stop() {
        this.configCenter.stop();
    }
}
