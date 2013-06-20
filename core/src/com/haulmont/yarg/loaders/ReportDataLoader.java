package com.haulmont.yarg.loaders;

import com.haulmont.yarg.structure.impl.BandData;
import com.haulmont.yarg.structure.ReportQuery;

import java.util.List;
import java.util.Map;


/**
 * Data loader is stateless bean which load rows of data depending on data set, parent band and params
 */
public interface ReportDataLoader {
    List<Map<String, Object>> loadData(ReportQuery reportQuery, BandData parentBand, Map<String, Object> params);
}