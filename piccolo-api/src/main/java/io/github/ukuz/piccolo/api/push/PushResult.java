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
package io.github.ukuz.piccolo.api.push;

import io.github.ukuz.piccolo.api.router.ClientLocator;
import lombok.Builder;
import lombok.Getter;

/**
 * @author ukuz90
 */
@Builder
@Getter
public class PushResult {

    public static final byte CODE_SUCCESS = 1;
    public static final byte CODE_FAILURE = 2;
    public static final byte CODE_OFFLINE = 3;
    public static final byte CODE_TIMEOUT = 4;

    private byte resultCode;
    private String userId;
    private Object[] timeline;
    private ClientLocator clientLocator;



}
