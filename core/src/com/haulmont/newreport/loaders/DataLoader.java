package com.haulmont.newreport.loaders;

import com.haulmont.newreport.structure.impl.Band;
import com.haulmont.newreport.structure.DataSet;

import java.util.List;
import java.util.Map;


/**
 * Data loader is stateless bean which load rows of data depending on data set, parent band and params
 */
public interface DataLoader {
    List<Map<String, Object>> loadData(DataSet dataSet, Band parentBand, Map<String, Object> params);
}