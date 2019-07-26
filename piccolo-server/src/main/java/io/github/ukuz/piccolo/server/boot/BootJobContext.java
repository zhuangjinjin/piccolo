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

/**
 * @author ukuz90
 */
public class BootJobContext {

    private final BootJob job;
    private BootJobContext prev;
    private BootJobContext next;

    public BootJobContext(BootJob job) {
        this.job = job;
    }

    public BootJobContext getPrev() {
        return prev;
    }

    public void setPrev(BootJobContext prev) {
        this.prev = prev;
    }

    public BootJobContext getNext() {
        return next;
    }

    public void setNext(BootJobContext next) {
        this.next = next;
    }

    public BootJob getJob() {
        return job;
    }
}
