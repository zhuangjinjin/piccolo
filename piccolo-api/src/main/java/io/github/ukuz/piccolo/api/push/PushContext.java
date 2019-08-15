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
package io.github.ukuz.piccolo.api.push;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.util.List;
import java.util.Set;

/**
 * @author ukuz90
 */
@Builder
@Getter
public class PushContext {

    /**
     * 待推送的内容
     */
    private byte[] context;

    /**
     * 待推送的消息
     */
    private PushMsg pushMsg;

    /**
     * 目标用户
     */
    private String userId;

    /**
     * 目标用户,批量
     */
    private List<String> userIds;

    /**
     * 消息ack模式
     */
    private AckModel ackModel = AckModel.NO_ACK;

    /**
     * 推送成功后的回调
     */
    private PushCallback callback;

    /**
     * 推送超时时间
     */
    private int timeout = 3000;

    //================================broadcast=====================================//

    /**
     * 全网广播在线用户
     */
    private boolean broadcast = false;

    /**
     * 用户标签过滤
     */
    private Set<String> tags;

}
