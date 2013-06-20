package com.haulmont.yarg.formatters;

import com.haulmont.yarg.structure.Report;
import com.haulmont.yarg.structure.impl.BandData;

import java.util.Map;

public interface CustomReport {
    byte[] createReport(Report report, BandData rootBand, Map<String, Object> params);
}
