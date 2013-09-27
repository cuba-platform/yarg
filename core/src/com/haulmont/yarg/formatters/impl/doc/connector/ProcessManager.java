/*
 * Copyright (c) 2013 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.yarg.formatters.impl.doc.connector;

import java.util.List;

/**
 * @author subbotin
 * @version $Id$
 */
public interface ProcessManager {

    public static final long PID_UNKNOWN = -1;

    List<Long> findPid(String host, int port);

    void kill(Process process, List<Long> pid);
}
