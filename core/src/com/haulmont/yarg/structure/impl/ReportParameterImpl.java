/**
 *
 * @author degtyarjov
 * @version $Id$
 */
package com.haulmont.yarg.structure.impl;

import com.haulmont.yarg.structure.ReportParameter;

public class ReportParameterImpl implements ReportParameter {

    protected String name;
    protected String alias;
    protected Boolean required;
    protected Class paramClass;

    public ReportParameterImpl(String name, String alias, Boolean required, Class paramClass) {
        this.name = name;
        this.alias = alias;
        this.required = required;
        this.paramClass = paramClass;
    }

    public String getName() {
        return name;
    }

    public String getAlias() {
        return alias;
    }

    public Boolean getRequired() {
        return required;
    }

    public Class getParameterClass() {
        return paramClass;
    }
}
