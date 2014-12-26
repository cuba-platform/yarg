package com.haulmont.yarg.structure;

/**
 * @author degtyarjov
 * @version $Id$
 */
public interface ReportParameterWithDefaultValue extends ReportParameter {
    /**
     * @return default value of the parameter, if exists
     */
    String getDefaultValue();
}
