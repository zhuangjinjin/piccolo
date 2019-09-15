/*
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
 * This MBean represents a connection
 *
 * Copied from zookeeper
 */
public interface ConnectionMXBean {

    /**
     * get server bind port
     * @return
     */
    int getBindPort();

    /**
     * the time of server was started
     * @return
     */
    long getStartTime();

    /**
     * min request latency in ms
     * @return
     */
    long getMinRequestLatency();

    /**
     * avg request latency in ms
     * @return
     */
    long getAvgRequestLatency();

    /**
     * max request latency in ms
     * @return
     */
    long getMaxRequestLatency();

    /**
     * number of packets received so far
     * @return
     */
    long getPacketReceived();

    /**
     * number of packets sent so far
     * @return
     */
    long getPacketSent();

    /**
     *
     * @return
     */
    long getOutstandingRequests();


}
