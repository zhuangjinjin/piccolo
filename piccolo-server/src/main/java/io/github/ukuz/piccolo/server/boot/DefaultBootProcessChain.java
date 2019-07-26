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
public class DefaultBootProcessChain implements BootProcessChain {

    private BootJobContext header = newBootJobContext(new EmptyBootJob());
    private BootJobContext tail = newBootJobContext(new EmptyBootJob());

    public DefaultBootProcessChain() {
        header.setNext(tail);
        tail.setPrev(header);
    }

    @Override
    public void start() {
        BootJobContext cursor = header;
        while (cursor != null) {
            cursor.getJob().start();
            cursor = cursor.getNext();
        }
    }

    @Override
    public void stop() {
        BootJobContext cursor = tail;
        while (cursor != null) {
            cursor.getJob().stop();
            cursor = cursor.getPrev();
        }
    }

    @Override
    public BootProcessChain addLast(BootJob bootJob) {
        BootJobContext newJob = newBootJobContext(bootJob);

        tail.getPrev().setNext(newJob);
        newJob.setPrev(tail.getPrev());

        tail.setPrev(newJob);
        newJob.setNext(tail);

        return this;
    }

    @Override
    public BootProcessChain addFirst(BootJob bootJob) {
        BootJobContext newJob = newBootJobContext(bootJob);

        header.getNext().setPrev(newJob);
        newJob.setNext(header.getNext());

        header.setNext(newJob);
        newJob.setPrev(header);

        return this;
    }

    private BootJobContext newBootJobContext(BootJob bootJob) {
        return new BootJobContext(bootJob);
    }

    private static class EmptyBootJob implements BootJob {
    }

}
