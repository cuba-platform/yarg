package com.haulmont.newreport.formatters.impl.doc.connector;

import java.util.concurrent.ExecutorService;

public interface OOConnectorAPI {
    OOConnection createConnection() throws NoFreePortsException;

    ExecutorService getExecutor();

    void openConnectionRunWithTimeoutAndClose(OOConnection connection, Runnable runnable, int timeoutInSeconds);
}
