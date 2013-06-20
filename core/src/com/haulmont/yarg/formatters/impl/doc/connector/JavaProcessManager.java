/*
 * Copyright (c) 2013 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.yarg.formatters.impl.doc.connector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

/**
 * @author subbotin
 * @version $Id$
 */
class JavaProcessManager implements ProcessManager {
    protected static final Logger log = LoggerFactory.getLogger(JavaProcessManager.class);

    @Override
    public List<Long> findPid(String host, int port) {
        return Collections.singletonList(PID_UNKNOWN);
    }

    @Override
    public void kill(Process process, List<Long> pids) {
        log.info("Java office process manager is going to kill following processes " + pids);
        if (process != null)
            process.destroy();
    }
}
