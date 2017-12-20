/*
 * Copyright 2013 Haulmont
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.haulmont.yarg.formatters.impl.doc.connector;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.haulmont.yarg.exception.OpenOfficeException;
import com.haulmont.yarg.exception.ReportingInterruptedException;
import com.sun.star.comp.helper.BootstrapException;

import java.util.Set;
import java.util.concurrent.*;

public class OfficeIntegration implements OfficeIntegrationAPI {
    protected volatile boolean platformDependProcessManagement = true;
    protected final ExecutorService executor;
    protected final BlockingQueue<OfficeConnection> connectionsQueue = new LinkedBlockingDeque<OfficeConnection>();
    protected final Set<OfficeConnection> connections = new CopyOnWriteArraySet<OfficeConnection>();

    protected String openOfficePath;
    protected String temporaryDirPath;
    protected Integer[] openOfficePorts;
    protected Integer timeoutInSeconds = 60;
    protected int countOfRetry = 2;
    protected Boolean displayDeviceAvailable = false;

    public OfficeIntegration(String openOfficePath, Integer... ports) {
        this.openOfficePath = openOfficePath;
        this.openOfficePorts = ports;
        initConnections(ports);
        executor = createExecutor();
    }

    public void setTemporaryDirPath(String temporaryDirPath) {
        this.temporaryDirPath = temporaryDirPath;
    }

    public void setTimeoutInSeconds(Integer timeoutInSeconds) {
        this.timeoutInSeconds = timeoutInSeconds;
    }

    public void setDisplayDeviceAvailable(Boolean displayDeviceAvailable) {
        this.displayDeviceAvailable = displayDeviceAvailable;
    }

    public void setCountOfRetry(int countOfRetry) {
        this.countOfRetry = countOfRetry;
    }

    public String getTemporaryDirPath() {
        return temporaryDirPath;
    }

    public Integer getTimeoutInSeconds() {
        return timeoutInSeconds;
    }

    public Boolean isDisplayDeviceAvailable() {
        return displayDeviceAvailable;
    }

    @Override
    public void runTaskWithTimeout(final OfficeTask officeTask, int timeoutInSeconds) throws NoFreePortsException {
        final OfficeConnection connection = acquireConnection();
        Future future = null;
        try {
            Callable<Void> task = () -> {
                connection.open();
                officeTask.processTaskInOpenOffice(connection.getOOResourceProvider());
                return null;
            };
            future = executor.submit(task);
            future.get(timeoutInSeconds, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            throw new ReportingInterruptedException("Open office task interrupted");
        } catch (ExecutionException ex) {
            connection.close();
            if (ex.getCause() instanceof BootstrapException) {
                throw new OpenOfficeException("Failed to connect to open office. Please check open office path " + openOfficePath, ex);
            }
            throw new RuntimeException(ex.getCause());
        } catch (TimeoutException tex) {
            try {
                if (Thread.interrupted()) {
                    throw new ReportingInterruptedException("Open office task interrupted");
                }
            } finally {
                connection.close();
            }
            if (tex.getCause() instanceof BootstrapException) {
                throw new OpenOfficeException("Failed to connect to open office. Please check open office path " + openOfficePath, tex);
            }
            throw new OpenOfficeException(tex);
        } catch (Throwable ex) {
            connection.close();
            if (ex.getCause() instanceof BootstrapException) {
                throw new OpenOfficeException("Failed to connect to open office. Please check open office path " + openOfficePath, ex);
            }
            throw new OpenOfficeException(ex);
        } finally {
            if (future != null) {
                future.cancel(true);
            }
            releaseConnection(connection);
        }
    }

    public int getCountOfRetry() {
        return countOfRetry;
    }

    public ExecutorService getExecutor() {
        return executor;
    }

    public String getAvailablePorts() {
        StringBuilder builder = new StringBuilder();

        Integer[] ports = new Integer[connections.size()];
        int i = 0;
        for (OfficeConnection officeConnection : connectionsQueue) {
            ports[i] = officeConnection.port;
        }


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
        for (OfficeConnection connection : connections) {
            connection.close();
        }

        connectionsQueue.clear();
        connectionsQueue.addAll(connections);
    }

    public boolean getPlatformDependProcessManagement() {
        return platformDependProcessManagement;
    }

    public void setPlatformDependProcessManagement(boolean platformDependProcessManagement) {
        this.platformDependProcessManagement = platformDependProcessManagement;
    }

    protected ExecutorService createExecutor() {
        return Executors.newFixedThreadPool(connections.size(),
                new ThreadFactoryBuilder()
                        .setNameFormat("OfficeIntegration-%d")
                        .build());
    }

    protected OfficeConnection acquireConnection() throws NoFreePortsException {
        final OfficeConnection connection = connectionsQueue.poll();
        if (connection != null) {
            return connection;
        } else {
            throw new NoFreePortsException("Couldn't get free port from pool");
        }
    }

    protected void releaseConnection(OfficeConnection officeConnection) {
        connectionsQueue.add(officeConnection);
    }

    protected void initConnections(Integer[] ports) {
        for (Integer port : ports) {
            connections.add(createConnection(port));
        }

        connectionsQueue.addAll(connections);
    }

    protected OfficeConnection createConnection(Integer port) {
        return new OfficeConnection(openOfficePath, port, resolveProcessManager(), this);
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
}
