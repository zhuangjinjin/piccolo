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
package io.github.ukuz.piccolo.client.connect;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author ukuz90
 */
public final class SimpleStatistics {

    private final AtomicInteger connectedNum = new AtomicInteger(0);
    private final AtomicInteger clientNum = new AtomicInteger(0);
    private final AtomicInteger bindUserNum = new AtomicInteger(0);
    private final AtomicInteger receivePushNum = new AtomicInteger(0);

    public int increaseConnectedNum() {
        return connectedNum.incrementAndGet();
    }

    public int increaseClientNum() {
        return clientNum.incrementAndGet();
    }

    public int increaseBindUserNum() {
        return bindUserNum.incrementAndGet();
    }

    public int increaseReceivePushNum() {
        return receivePushNum.incrementAndGet();
    }

    @Override
    public String toString() {
        return "SimpleStatistics{" +
                "connectedNum=" + connectedNum +
                ", clientNum=" + clientNum +
                ", bindUserNum=" + bindUserNum +
                ", receivePushNum=" + receivePushNum +
                '}';
    }
}
