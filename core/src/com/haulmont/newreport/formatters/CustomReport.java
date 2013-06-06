package com.haulmont.newreport.formatters;

import com.haulmont.newreport.structure.Report;
import com.haulmont.newreport.structure.impl.Band;

import java.util.Map;

public interface CustomReport {
    byte[] createReport(Report report, Band rootBand, Map<String, Object> params);
}
