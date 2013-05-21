package com.haulmont.newreport.formatters.impl.doc.connector;

public interface OfficeIntegrationAPI {
    Integer getTimeoutInSeconds();

    Boolean isDisplayDeviceAvailable();

    void runTaskWithTimeout(OfficeTask officeTask, int timeoutInSeconds) throws NoFreePortsException;
}
