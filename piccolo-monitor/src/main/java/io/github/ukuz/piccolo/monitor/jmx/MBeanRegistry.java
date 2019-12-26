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
package io.github.ukuz.piccolo.monitor.jmx;

import io.github.ukuz.piccolo.monitor.jmx.mxbean.PiccoloMBeanInfo;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import java.lang.management.ManagementFactory;
import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * @author ukuz90
 */
public class MBeanRegistry {

    private MBeanServer mBeanServer;
    private static volatile MBeanRegistry instance;

    public static MBeanRegistry getInstance() {
        if (instance == null) {
            synchronized (MBeanRegistry.class) {
                if (instance == null) {
                    instance = new MBeanRegistry();
                }
            }
        }
        return instance;
    }

    private MBeanRegistry() {
        try {
            mBeanServer = ManagementFactory.getPlatformMBeanServer();
        } catch (SecurityException e) {
            mBeanServer = AccessController.doPrivileged((PrivilegedAction<MBeanServer>) MBeanServerFactory::createMBeanServer);
        }
    }

    public void registerMBeanInfo(PiccoloMBeanInfo mBeanInfo) {
    }

}
