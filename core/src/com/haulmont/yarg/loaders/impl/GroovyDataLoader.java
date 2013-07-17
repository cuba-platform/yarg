/*
 * Copyright (c) 2008 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */
package com.haulmont.yarg.loaders.impl;

import com.haulmont.yarg.loaders.ReportDataLoader;
import com.haulmont.yarg.structure.ReportQuery;
import com.haulmont.yarg.structure.BandData;
import com.haulmont.yarg.util.groovy.Scripting;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroovyDataLoader implements ReportDataLoader {
    private Scripting scripting;

    public GroovyDataLoader(Scripting scripting) {
        this.scripting = scripting;
    }

    @Override
    public List<Map<String, Object>> loadData(ReportQuery reportQuery, BandData parentBand, Map<String, Object> params) {
        String script = reportQuery.getScript();
        Map<String, Object> scriptParams = new HashMap<String, Object>();
        scriptParams.put("reportQuery", reportQuery);
        scriptParams.put("parentBand", parentBand);
        scriptParams.put("params", params);
        return scripting.evaluateGroovy(script, scriptParams);
    }
}
