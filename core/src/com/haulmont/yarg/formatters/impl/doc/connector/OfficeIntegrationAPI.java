package com.haulmont.yarg.formatters.impl.doc.connector;

public interface OfficeIntegrationAPI {
    Integer getTimeoutInSeconds();

    Boolean isDisplayDeviceAvailable();

    void runTaskWithTimeout(OfficeTask officeTask, int timeoutInSeconds) throws NoFreePortsException;
}
