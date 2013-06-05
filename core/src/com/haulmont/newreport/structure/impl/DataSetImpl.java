/**
 *
 * @author degtyarjov
 * @version $Id$
 */
package com.haulmont.newreport.structure.impl;

import com.haulmont.newreport.structure.DataSet;

import java.util.Collections;
import java.util.Map;

public class DataSetImpl implements DataSet {
    protected String name;

    protected String script;

    protected String loaderType;

    protected Map<String, Object> additionalParams = Collections.emptyMap();

    public DataSetImpl(String name, String script, String loaderType) {
        this.name = name;
        this.script = script;
        this.loaderType = loaderType;
    }

    public DataSetImpl(String name, String script, String loaderType, Map<String, Object> additionalParams) {
        this.name = name;
        this.script = script;
        this.loaderType = loaderType;
        this.additionalParams = additionalParams;
    }

    public DataSetImpl(DataSet dataSet) {
        name = dataSet.getName();
        script = dataSet.getScript();
        loaderType = dataSet.getLoaderType();
    }

    public String getScript() {
        return script;
    }

    public String getLoaderType() {
        return loaderType;
    }

    public String getName() {
        return name;
    }

    @Override
    public Map<String, Object> getAdditionalParams() {
        return Collections.unmodifiableMap(additionalParams);
    }
}
