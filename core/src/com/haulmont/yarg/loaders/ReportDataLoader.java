package com.haulmont.yarg.loaders;

import com.haulmont.yarg.structure.BandData;
import com.haulmont.yarg.structure.ReportQuery;

import java.util.List;
import java.util.Map;


/**
 * This interface describes a logic which load rows of data using report query, parent band and params
 */
public interface ReportDataLoader {
    List<Map<String, Object>> loadData(ReportQuery reportQuery, BandData parentBand, Map<String, Object> params);
}