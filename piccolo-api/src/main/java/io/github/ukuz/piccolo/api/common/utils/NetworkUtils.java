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
package io.github.ukuz.piccolo.api.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.regex.Pattern;

/**
 * @author ukuz90
 */
public final class NetworkUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(NetworkUtils.class);
    private static String extranetAddress;
    private static String localAddress;
    private static final Pattern LOCAL_IP_PATTERN = Pattern.compile("127(\\.\\d{1,3}){3}$");

    private NetworkUtils() {}

    public static NetworkInterface getLocalNetworkInterface() {
        Enumeration<NetworkInterface> interfaces = null;
        try {
            interfaces = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            LOGGER.error("getLocalNetworkInterface failure, cause: {}", e);
            throw new RuntimeException("NetworkInterface not found", e);
        }
        while(interfaces.hasMoreElements()) {
            NetworkInterface networkInterface = interfaces.nextElement();
            Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
            while (addresses.hasMoreElements()) {
                InetAddress address = addresses.nextElement();
                if (address.isLoopbackAddress()) {
                    continue;
                }
                if (address.getHostAddress().contains(":")) {
                    continue;
                }
                if (address.isSiteLocalAddress()) {
                    return networkInterface;
                }
            }
        }
        throw new RuntimeException("NetworkInterface not found");
    }

    private static String getInetAddress(boolean local) {
        try {
            Enumeration<NetworkInterface> interfaces =  NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                Enumeration<InetAddress> addresses = interfaces.nextElement().getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    if (address.isLoopbackAddress()) {
                        continue;
                    }
                    if (address.getHostAddress().contains(":")) {
                        continue;
                    }

                    if (local) {
                        if (address.isSiteLocalAddress()) {
                            return address.getHostAddress();
                        }
                    } else {
                        if (!address.isSiteLocalAddress()) {
                            return address.getHostAddress();
                        }
                    }
                }
            }
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("getInetAddress is null, local: {}", local);
            }
            return local ? "127.0.0.1" : null;
        } catch (SocketException e) {
            LOGGER.error("getInetAddress failure, cause: {}", e);
            return local ? "127.0.0.1" : null;
        }
    }

    public static String getExtranetAddress() {
        if (extranetAddress == null) {
            extranetAddress = getInetAddress(false);
        }
        return extranetAddress;
    }

    public static String getLocalAddress() {
        if (localAddress == null) {
            localAddress = getInetAddress(true);
        }
        return localAddress;
    }

    public static boolean isLocalHost(String host) {
        return host == null
                || host.length() == 0
                || host.equalsIgnoreCase("localhost")
                || host.equals("0.0.0.0")
                || LOCAL_IP_PATTERN.matcher(host).matches();
    }

}
