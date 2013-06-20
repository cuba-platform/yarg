/*
 * Copyright (c) 2013 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.yarg.formatters.impl.doc.connector;


import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.lang.StringUtils.isNotBlank;

/**
 * @author subbotin
 * @version $Id$
 */
class WinProcessManager implements ProcessManager {
    protected static final Logger log = LoggerFactory.getLogger(JavaProcessManager.class);

    private static final String KILL_COMMAND = "taskkill /f /PID %s";
    private static final String FIND_PID_COMMAND = "cmd /c netstat -a -n -o -p TCP|findstr \"%s\"";
    private static final Pattern NETSTAT_PATTERN =
            Pattern.compile("^.*?(\\d+\\.\\d+\\.\\d+\\.\\d+)[\\.\\:](\\d+)\\s+(\\d+\\.\\d+\\.\\d+\\.\\d+)[\\.\\:](\\d+)\\s+\\w+\\s+(\\d+)");
    private static final String LOCAL_HOST = "127.0.0.1";



    protected static class NetStatInfo {
        private String localAddress;
        private int localPort;
        private long pid;

        private NetStatInfo(String output) {
            Matcher matcher = NETSTAT_PATTERN.matcher(output);
            if (matcher.matches()) {
                localAddress = matcher.group(1);
                String value = matcher.group(2);
                if (isNotBlank(value))
                    localPort = Integer.valueOf(value);
                value = matcher.group(5);
                if (isNotBlank(value))
                    pid = Long.valueOf(value);
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Long> findPid(String host, int port) {
        try {
            if ("localhost".equalsIgnoreCase(host))
                host = LOCAL_HOST;
            Process process = Runtime.getRuntime().exec(String.format(FIND_PID_COMMAND, port));
            List r = IOUtils.readLines(process.getInputStream());
            for (Object output : r) {
                NetStatInfo info = new NetStatInfo((String) output);
                if (info.localPort == port && ObjectUtils.equals(host, info.localAddress))
                    return Collections.singletonList(info.pid);
            }
        } catch (IOException e) {
            log.warn(String.format("Unable to find PID for OO process on host:port  %s:%s", host, port), e);
        }
        log.warn(String.format("Unable to find PID for OO process on host:port %s:%s", host, port));
        return Collections.singletonList(PID_UNKNOWN);
    }

    @Override
    public void kill(Process process, List<Long> pids) {
        for (Long pid : pids) {
            if (PID_UNKNOWN == pid) {
                if (process != null) {
                    process.destroy();
                }
            } else {
                String command = String.format(KILL_COMMAND, pid);
                try {
                    Runtime.getRuntime().exec(command);
                } catch (IOException e) {
                    log.warn(String.format("Unable to kill OO process id=%s", pid), e);
                }
            }
        }
    }
}
