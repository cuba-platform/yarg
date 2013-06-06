/*
 * Copyright (c) 2010 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Vasiliy Fontanenko
 * Created: 12.10.2010 19:21:08
 *
 * $Id$
 */
package com.haulmont.newreport.formatters.impl.doc.connector;

import com.haulmont.newreport.exception.OpenOfficeException;
import com.sun.star.comp.helper.BootstrapException;

import java.util.Collections;
import java.util.concurrent.*;

public class OfficeIntegration implements OfficeIntegrationAPI {
    protected volatile boolean platformDependProcessManagement = true;
    protected final ExecutorService executor;
    protected final BlockingQueue<Integer> freePorts = new LinkedBlockingDeque<Integer>();
    protected Integer[] openOfficePorts;
    protected String openOfficePath;
    protected Integer timeoutInSeconds = 60;
    protected Boolean displayDeviceAvailable = false;

    public OfficeIntegration(String openOfficePath, Integer... ports) {
        this.openOfficePath = openOfficePath;
        this.openOfficePorts = ports;
        Collections.addAll(freePorts, ports);
        executor = Executors.newFixedThreadPool(freePorts.size());
    }

    public void setTimeoutInSeconds(Integer timeoutInSeconds) {
        this.timeoutInSeconds = timeoutInSeconds;
    }

    public void setDisplayDeviceAvailable(Boolean displayDeviceAvailable) {
        this.displayDeviceAvailable = displayDeviceAvailable;
    }

    public Integer getTimeoutInSeconds() {
        return timeoutInSeconds;
    }

    public Boolean isDisplayDeviceAvailable() {
        return displayDeviceAvailable;
    }

    @Override
    public void runTaskWithTimeout(final OfficeTask officeTask, int timeoutInSeconds) throws NoFreePortsException {
        final OfficeConnection connection = createConnection();
        Future future = null;
        try {
            Callable<Void> task = new Callable<Void>() {
                @Override
                public Void call() throws java.lang.Exception {
                    officeTask.processTaskInOpenOffice(connection.getOOResourceProvider());
                    connection.close();
                    return null;
                }
            };
            future = executor.submit(task);
            future.get(timeoutInSeconds, TimeUnit.SECONDS);
        } catch (ExecutionException ex) {
            if (ex.getCause() instanceof BootstrapException) {
                throw new OpenOfficeException("Failed to connect to open office. Please check open office path " + openOfficePath, ex);
            }
            throw new RuntimeException(ex.getCause());
        } catch (java.lang.Exception ex) {
            throw new OpenOfficeException(ex);
        } finally {
            if (future != null) {
                future.cancel(true);
            }
            connection.releaseResources();
        }
    }

    public ExecutorService getExecutor() {
        return executor;
    }

    public String getAvailablePorts() {
        StringBuilder builder = new StringBuilder();

        Integer[] ports = freePorts.toArray(new Integer[freePorts.size()]);

        if ((ports.length > 0)) {
            for (Integer port : ports) {
                if (port != null)
                    builder.append(Integer.toString(port)).append(" ");
            }
        } else {
            builder.append("No available ports");
        }

        return builder.toString();
    }

    public void hardReloadAccessPorts() {
        freePorts.clear();
        Collections.addAll(this.freePorts, openOfficePorts);
    }

    public boolean getPlatformDependProcessManagement() {
        return platformDependProcessManagement;
    }

    public void setPlatformDependProcessManagement(boolean platformDependProcessManagement) {
        this.platformDependProcessManagement = platformDependProcessManagement;
    }

    protected OfficeConnection createConnection() throws NoFreePortsException {
        final Integer port = freePorts.poll();
        if (port != null) {
            return new OfficeConnection(openOfficePath, port, resolveProcessManager(), this);
        } else {
            throw new NoFreePortsException("Couldn't get free port from pool");
        }
    }

    protected ProcessManager resolveProcessManager() {
        if (platformDependProcessManagement) {
            String os = System.getProperty("os.name").toLowerCase();
            if (os.startsWith("windows"))
                return new WinProcessManager();
            if (os.startsWith("linux"))
                return new LinuxProcessManager();
        }
        return new JavaProcessManager();
    }

    void putPortBack(Integer port) {
        freePorts.add(port);
    }
}
