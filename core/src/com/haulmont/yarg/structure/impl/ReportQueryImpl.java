/**
 *
 * @author degtyarjov
 * @version $Id$
 */
package com.haulmont.yarg.structure.impl;

import com.haulmont.yarg.structure.ReportQuery;

import java.util.Collections;
import java.util.Map;

public class ReportQueryImpl implements ReportQuery {
    protected String name;

    protected String script;

    protected String loaderType;

    protected Map<String, Object> additionalParams = Collections.emptyMap();

    public ReportQueryImpl(String name, String script, String loaderType) {
        this.name = name;
        this.script = script;
        this.loaderType = loaderType;
    }

    public ReportQueryImpl(String name, String script, String loaderType, Map<String, Object> additionalParams) {
        this.name = name;
        this.script = script;
        this.loaderType = loaderType;
        this.additionalParams = additionalParams;
    }

    public ReportQueryImpl(ReportQuery reportQuery) {
        name = reportQuery.getName();
        script = reportQuery.getScript();
        loaderType = reportQuery.getLoaderType();
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
