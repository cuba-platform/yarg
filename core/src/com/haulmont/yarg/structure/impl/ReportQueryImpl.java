/**
 *
 * @author degtyarjov
 * @version $Id$
 */
package com.haulmont.yarg.structure.impl;

import com.google.common.base.Preconditions;
import com.haulmont.yarg.structure.ReportQuery;

import java.util.Collections;
import java.util.Map;

public class ReportQueryImpl implements ReportQuery {
    protected String name;

    protected String linkParameterName;

    protected String script;

    protected String loaderType;

    protected Map<String, Object> additionalParams = Collections.emptyMap();

    public ReportQueryImpl(String name, String script, String loaderType, String linkParameterName, Map<String, Object> additionalParams) {
        this.name = name;
        this.script = script;
        this.loaderType = loaderType;
        this.additionalParams = additionalParams;
        this.linkParameterName = linkParameterName;
        validate();
    }

    public ReportQueryImpl(ReportQuery reportQuery) {
        this(reportQuery.getName(), reportQuery.getScript(), reportQuery.getLoaderType(), reportQuery.getLinkParameterName(), reportQuery.getAdditionalParams());
    }

    private void validate() {
        Preconditions.checkNotNull(this.name, "\"name\" parameter can not be null");
        Preconditions.checkNotNull(this.script, "\"script\" parameter can not be null");
        Preconditions.checkNotNull(this.loaderType, "\"loaderType\" parameter can not be null");
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

    public String getLinkParameterName() {
        return linkParameterName;
    }

    @Override
    public Map<String, Object> getAdditionalParams() {
        return Collections.unmodifiableMap(additionalParams);
    }
}
