package com.haulmont.newreport.formatters.impl.doc.connector;

public interface OOTaskRunnerAPI {
    Integer getTimeoutInSeconds();

    void runTaskWithTimeout(OfficeTask officeTask, int timeoutInSeconds) throws NoFreePortsException;
}
