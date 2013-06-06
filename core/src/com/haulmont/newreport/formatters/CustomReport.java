package com.haulmont.newreport.formatters;

import com.haulmont.newreport.structure.Report;

import java.util.Map;

public interface CustomReport {
    byte[] createReport(Report report, Map<String, Object> params);
}
