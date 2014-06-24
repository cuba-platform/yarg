/*
 * Copyright 2013 Haulmont
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

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
