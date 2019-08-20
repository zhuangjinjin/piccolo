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
package io.github.ukuz.piccolo.core.router;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import io.github.ukuz.piccolo.api.event.UserOfflineEvent;
import io.github.ukuz.piccolo.api.event.UserOnlineEvent;
import io.github.ukuz.piccolo.api.mq.MQClient;
import io.github.ukuz.piccolo.common.event.EventObservable;
import static io.github.ukuz.piccolo.mq.kafka.Topics.ONLINE_MESSAGE;
import static io.github.ukuz.piccolo.mq.kafka.Topics.OFFLINE_MESSAGE;

/**
 * @author ukuz90
 */
public class UserEventListener extends EventObservable {

    private MQClient mqClient;

    public UserEventListener(MQClient mqClient) {
        this.mqClient = mqClient;
    }

    @Subscribe
    @AllowConcurrentEvents
    public void on(UserOnlineEvent event) {
        mqClient.publish(ONLINE_MESSAGE.getTopic(), event.getUserId());
    }

    @Subscribe
    @AllowConcurrentEvents
    public void on(UserOfflineEvent event) {
        mqClient.publish(OFFLINE_MESSAGE.getTopic(), event.getUserId());
    }

}
