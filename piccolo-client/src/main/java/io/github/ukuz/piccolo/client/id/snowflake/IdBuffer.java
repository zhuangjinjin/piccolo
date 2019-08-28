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
package io.github.ukuz.piccolo.client.id.snowflake;

import io.github.ukuz.piccolo.api.external.common.Assert;
import io.github.ukuz.piccolo.api.id.IdGenException;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author ukuz90
 */
public class IdBuffer {

    private final int capacity;
    private final int threshold;

    private final long[] data;
    private AtomicInteger readIndex;
    private volatile int writeIndex;

    public IdBuffer(int capacity, int threshold) {
        this.capacity = capacity;
        this.threshold = threshold;
        this.data = new long[capacity];
        this.readIndex = new AtomicInteger(0);
    }

    public long read() throws IdGenException {
        if (canNotRead()) {
            throw new IdGenException("No id to read");
        }

        long id;
        int r;
        do {
            r = readIndex.get();
            if (r >= capacity) {
                throw new IdGenException("No id to read");
            }
            id = data[r];
        } while (!readIndex.compareAndSet(r, r + 1));

        return id;
    }

    public synchronized void write(long[] data) {
        Assert.isTrue(data.length == capacity, "data's length invalid, len:" + data.length);
        Assert.isTrue(canNotRead(), "have id not read");
        int r = readIndex.get();
        for (int i = 0; i < data.length; i++) {
            this.data[i] = data[i];
        }
        readIndex.compareAndSet(r, 0);
        writeIndex = capacity;
    }

    public boolean isWarn() {
        return readIndex.get() >= threshold;
    }

    public boolean canNotRead() {
        return readIndex.get() == writeIndex;
    }

    @Override
    public String toString() {
        return "IdBuffer{" +
                "data=" + data.length +
                ", readIndex=" + readIndex +
                ", writeIndex=" + writeIndex +
                '}';
    }
}
