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
package io.github.ukuz.piccolo.mq.kafka;

/**
 * @author ukuz90
 */
public enum Topics {

    ;

    private final String topic;
    private final int numOfPartition;
    private final short replicationFactor;

    Topics(String topic, int numOfPartition, short replicationFactor) {
        this.topic = topic;
        this.numOfPartition = numOfPartition;
        this.replicationFactor = replicationFactor;
    }

    public String getTopic() {
        return topic;
    }

    public int getNumOfPartition() {
        return numOfPartition;
    }

    public short getReplicationFactor() {
        return replicationFactor;
    }
}
