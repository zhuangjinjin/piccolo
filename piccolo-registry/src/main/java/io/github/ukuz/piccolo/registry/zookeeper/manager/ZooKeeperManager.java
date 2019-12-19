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
package io.github.ukuz.piccolo.registry.zookeeper.manager;

import io.github.ukuz.piccolo.api.external.common.Assert;
import io.github.ukuz.piccolo.api.service.AbstractService;
import io.github.ukuz.piccolo.api.service.ServiceException;
import io.github.ukuz.piccolo.common.thread.NamedThreadFactory;
import io.github.ukuz.piccolo.common.thread.ThreadNames;
import io.github.ukuz.piccolo.registry.zookeeper.properties.ZooKeeperProperties;
import io.netty.util.internal.StringUtil;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.CuratorFrameworkFactory.Builder;
import org.apache.curator.framework.api.ACLProvider;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author ukuz90
 */
public final class ZooKeeperManager extends AbstractService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZooKeeperManager.class);

    private CuratorFramework client;
    private ZooKeeperProperties zkProp;
    /**
     * local cache
     */
    private ZooKeeperDirectory directory;
    private final String watchPath;

    public ZooKeeperManager(ZooKeeperProperties zkProp, String watchPath) {
        Assert.notNull(zkProp, "zkProperties must not be null.");
        Assert.notEmptyString(watchPath, "watchPath must not be empty.");
        this.zkProp = zkProp;
        this.watchPath = watchPath;
    }

    @Override
    public void init() throws ServiceException {
        Builder builder= CuratorFrameworkFactory.builder()
                .connectString(zkProp.getHost())
                .sessionTimeoutMs(zkProp.getSessionTimeoutMs())
                .connectionTimeoutMs(zkProp.getConnectionTimeoutMs())
                .retryPolicy(new ExponentialBackoffRetry(zkProp.getBaseSleepTimeMs(), zkProp.getMaxRetries(), zkProp.getMaxSleepMs()))
                .namespace(zkProp.getNs())
                .threadFactory(new NamedThreadFactory(ThreadNames.T_ZK))
                ;

        // copied from baymax's ZKClient.java
        if (!StringUtil.isNullOrEmpty(zkProp.getDigest())) {
            /*
             * scheme对应于采用哪种方案来进行权限管理，zookeeper实现了一个pluggable的ACL方案，可以通过扩展scheme，来扩展ACL的机制。
             * zookeeper缺省支持下面几种scheme:
             *
             * world: 默认方式，相当于全世界都能访问; 它下面只有一个id, 叫anyone, world:anyone代表任何人，zookeeper中对所有人有权限的结点就是属于world:anyone的
             * auth: 代表已经认证通过的用户(cli中可以通过addauth digest user:pwd 来添加当前上下文中的授权用户); 它不需要id, 只要是通过authentication的user都有权限（zookeeper支持通过kerberos来进行authencation, 也支持username/password形式的authentication)
             * digest: 即用户名:密码这种方式认证，这也是业务系统中最常用的;它对应的id为username:BASE64(SHA1(password))，它需要先通过username:password形式的authentication
             * ip: 使用Ip地址认证;它对应的id为客户机的IP地址，设置的时候可以设置一个ip段，比如ip:192.168.1.0/16, 表示匹配前16个bit的IP段
             * super: 在这种scheme情况下，对应的id拥有超级权限，可以做任何事情(crwda) CREATE, READ, WRITE, DELETE, ADMIN
             */
            builder.authorization("digest", zkProp.getDigest().getBytes(StandardCharsets.UTF_8));
            builder.aclProvider(new ACLProvider() {
                @Override
                public List<ACL> getDefaultAcl() {
                    return ZooDefs.Ids.CREATOR_ALL_ACL;
                }

                @Override
                public List<ACL> getAclForPath(String path) {
                    return ZooDefs.Ids.CREATOR_ALL_ACL;
                }
            });
        }

        client = builder.build();
        LOGGER.info("{} init success, config: {}", getName(), zkProp);
    }

    @Override
    protected CompletableFuture<Boolean> doStartAsync() throws ServiceException {
        try {
            LOGGER.info("{} start", getName());
            client.start();
            LOGGER.info("{} wait for connect", getName());
            if (!client.blockUntilConnected(1, TimeUnit.MINUTES)) {
                throw new ZooKeeperInitialingException( getName() + " init failure, config: " + zkProp);
            }

            initLocalCache(watchPath);
            LOGGER.info("{} start success, server lists is: {}", getName(), zkProp.getHost());
            CompletableFuture future = new CompletableFuture();
            future.complete(true);
            return future;
        } catch (Throwable e) {
            throw new ServiceException(e);
        }

    }

    private void initLocalCache(String watchPath) throws Exception {
        directory = new ZooKeeperDirectory(client, watchPath);
        directory.start();
    }

    @Override
    public void destroy() throws ServiceException {
        directory.stop();
        client.close();
    }

    public ZooKeeperDirectory getDirectory() {
        return directory;
    }

    @Override
    protected String getName() {
        return "zookeeper manager";
    }
}
