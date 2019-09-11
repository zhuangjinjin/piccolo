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
package io.github.ukuz.piccolo.monitor.jmx.mxbean;

/**
 * @author ukuz90
 */
public interface ServerMXBean {

    /**
     * @return get server bind port
     */
    int getBindPort();

    /**
     * @return the time of server was started
     */
    long getStartTime();

    /**
     * @return min request latency in ms
     */
    long getMinRequestLatency();

    /**
     * @return avg request latency in ms
     */
    long getAvgRequestLatency();

    /**
     * @return max request latency in ms
     */
    long getMaxRequestLatency();

    /**
     * @return number of packets received so far
     */
    long getPacketReceived();

    /**
     * @return number of packets sent so far
     */
    long getPacketSent();


}
