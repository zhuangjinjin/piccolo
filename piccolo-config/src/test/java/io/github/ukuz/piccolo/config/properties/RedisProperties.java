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
package io.github.ukuz.piccolo.config.properties;

import io.github.ukuz.piccolo.api.config.ConfigurationProperties;
import io.github.ukuz.piccolo.api.config.Properties;

/**
 * @author ukuz90
 */
@ConfigurationProperties(prefix = "piccolo.redis")
public class RedisProperties implements Properties {

    private String mode;
    private int maxConnNum;
    private boolean enabled;
    private long timeBetweenEvictionRunsMillis;
    private short prop1;
    private float prop2;
    private double prop3;
    private byte prop4;

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public int getMaxConnNum() {
        return maxConnNum;
    }

    public void setMaxConnNum(int maxConnNum) {
        this.maxConnNum = maxConnNum;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public long getTimeBetweenEvictionRunsMillis() {
        return timeBetweenEvictionRunsMillis;
    }

    public void setTimeBetweenEvictionRunsMillis(long timeBetweenEvictionRunsMillis) {
        this.timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis;
    }

    public short getProp1() {
        return prop1;
    }

    public void setProp1(short prop1) {
        this.prop1 = prop1;
    }

    public float getProp2() {
        return prop2;
    }

    public void setProp2(float prop2) {
        this.prop2 = prop2;
    }

    public double getProp3() {
        return prop3;
    }

    public void setProp3(double prop3) {
        this.prop3 = prop3;
    }

    public byte getProp4() {
        return prop4;
    }

    public void setProp4(byte prop4) {
        this.prop4 = prop4;
    }
}
