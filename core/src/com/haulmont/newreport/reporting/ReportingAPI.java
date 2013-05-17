package com.haulmont.newreport.reporting;


import java.io.OutputStream;

public interface ReportingAPI {
    void runReport(RunParams runParams, OutputStream outputStream);

    byte[] runReport(RunParams runParams);
}
