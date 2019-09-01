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
package io.github.ukuz.piccolo.mq;

import io.github.ukuz.piccolo.api.mq.GenericMqMessage;
import io.github.ukuz.piccolo.api.mq.MQClient;

/**
 * @author ukuz90
 */
public abstract class CommittableMqMessage extends GenericMqMessage {

    protected MQClient mqClient;
    protected long xid;

    @Override
    public long getXid() {
        return xid;
    }

    @Override
    protected void doEncode() {
        writeLong(xid);
        doEncode0();
    }

    @Override
    protected void doDecode() {
        xid = readLong();
        doDecode0();
    }

    public void setXid(long xid) {
        this.xid = xid;
    }

    public void setMqClient(MQClient mqClient) {
        this.mqClient = mqClient;
    }

    protected abstract void doEncode0();

    protected abstract void doDecode0();
}
