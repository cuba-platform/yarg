/*
 * Copyright (c) 2008 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */
package com.haulmont.newreport.loaders.impl;

import com.haulmont.newreport.loaders.DataLoader;
import com.haulmont.newreport.structure.impl.Band;
import com.haulmont.newreport.structure.DataSet;
import com.haulmont.newreport.util.groovy.Scripting;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroovyDataLoader implements DataLoader {
    private Scripting scripting;

    public GroovyDataLoader(Scripting scripting) {
        this.scripting = scripting;
    }

    @Override
    public List<Map<String, Object>> loadData(DataSet dataSet, Band parentBand, Map<String, Object> params) {
        String script = dataSet.getScript();
        Map<String, Object> scriptParams = new HashMap<String, Object>();
        scriptParams.put("dataSet", dataSet);
        scriptParams.put("parentBand", parentBand);
        scriptParams.put("params", params);
        return scripting.evaluateGroovy(script, scriptParams);
    }
}
