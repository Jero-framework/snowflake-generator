/*
 * Copyright (c) 2017 Baidu, Inc. All Rights Reserve.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jero.snowflake.worker;

import com.jero.common.utils.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.management.ManagementFactory;

/**
 * Represents an implementation of {@link WorkerIdAssigner},
 * the worker id will be discarded after assigned to the UidGenerator
 *
 * @author yutianbao
 */
public class DefaultWorkerIdAssigner implements WorkerIdAssigner {

    private final Log log = LogFactory.getLog(DefaultWorkerIdAssigner.class);

    /**
     * 默认以pid构建当次的workerId
     *
     * @return assigned worker id
     */
    public long assignWorkerId() {
        // build worker id
        String workerIdStr = getPid();
        log.info("于" + System.currentTimeMillis() + "生成workerId:" + workerIdStr);

        if (!StringUtils.isNumber(workerIdStr)) {
            throw new IllegalArgumentException("获取Pid无效");
        }

        int workerId = Integer.valueOf(workerIdStr);
        return workerId;
    }

    /**
     * 获取当前启动进程Pid
     *
     * @return
     */
    public String getPid() {
        String name = ManagementFactory.getRuntimeMXBean().getName();
        String pid = name.split("@")[0];
        return pid;
    }

}
