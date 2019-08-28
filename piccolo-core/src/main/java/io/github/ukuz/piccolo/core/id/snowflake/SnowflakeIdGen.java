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
package io.github.ukuz.piccolo.core.id.snowflake;

import io.github.ukuz.piccolo.api.external.common.Assert;
import io.github.ukuz.piccolo.api.id.IdGen;
import io.github.ukuz.piccolo.api.id.IdGenException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ukuz90
 */
public class SnowflakeIdGen implements IdGen {

    private final static Logger LOGGER = LoggerFactory.getLogger(SnowflakeIdGen.class);

    private final static long TW_EPOCH = 1566918796000L;
    private final static long SEQ_BIT = 12L;
    private final static long SEQ_MAX = -1 ^ (-1 << SEQ_BIT);
    private final static long WORKER_ID_BIT = 10L;
    private final static long MAX_WORKER_ID = -1 ^ (-1 << WORKER_ID_BIT);
    private final static long TIMESTAMP_LEFT_SHIFT = SEQ_BIT + WORKER_ID_BIT;

    private long workerId;
    private long lastTimestamp;
    private long sequence;

    private final WorkerIdHolder workerIdHolder;

    public SnowflakeIdGen(WorkerIdHolder workerIdHolder) {
        Assert.notNull(workerIdHolder, "workerIdHolder must not be null");
        this.workerIdHolder = workerIdHolder;
    }

    @Override
    public boolean init() {
        workerIdHolder.init();
        workerId = workerIdHolder.getWorkerId();
        Assert.isTrue(workerId > 0 && workerId < MAX_WORKER_ID, "workerId invalid, workerId: " + workerId);
        return true;
    }

    @Override
    public boolean destroy() {
        return false;
    }

    @Override
    public synchronized long get(String tag) throws IdGenException {
        long timestamp = currentMillis();
        if (timestamp > lastTimestamp) {
            sequence = 1;
        } else if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) & SEQ_MAX;
            if (sequence == 0) {
                sequence = 1;
                timestamp = nextMillis();
            }
        } else {
            //时间回拨
            long offset = lastTimestamp - timestamp;
            if (offset <= 5) {
                try {
                    wait(offset << 1);
                    timestamp = currentMillis();
                    if (timestamp < lastTimestamp) {
                        throw new SnowflakeIdGenException("time exception");
                    }
                } catch (InterruptedException e) {
                    LOGGER.warn("wait interrupted, cause: {}", e);
                    throw new SnowflakeIdGenException("wait interrupted", e);
                }
            } else {
                throw new SnowflakeIdGenException("time exception");
            }
        }

        lastTimestamp = timestamp;
        return ((timestamp - TW_EPOCH) << TIMESTAMP_LEFT_SHIFT)
                | (workerId << SEQ_BIT)
                | sequence;
    }

    private long nextMillis() {
        long timestamp = currentMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = currentMillis();
        }
        return timestamp;
    }

    private long currentMillis() {
        return System.currentTimeMillis();
    }
}
