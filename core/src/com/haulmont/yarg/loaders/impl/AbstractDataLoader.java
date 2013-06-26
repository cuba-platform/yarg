/**
 *
 * @author degtyarjov
 * @version $Id$
 */
package com.haulmont.yarg.loaders.impl;

import com.haulmont.yarg.loaders.ReportFieldsConverter;
import com.haulmont.yarg.loaders.ReportDataLoader;
import com.haulmont.yarg.loaders.ReportParametersConverter;

public abstract class AbstractDataLoader implements ReportDataLoader {
    protected ReportParametersConverter parametersConverter = null;
    protected ReportFieldsConverter fieldsConverter = null;


    protected <T> T convertParameter(Object input) {
        if (parametersConverter != null) {
            return parametersConverter.convert(input);
        } else {
            return (T) input;
        }
    }

    protected <T> T convertOutputValue(Object input) {
        if (fieldsConverter != null) {
            return fieldsConverter.convert(input);
        } else {
            return (T) input;
        }
    }

    public void setParametersConverter(ReportParametersConverter reportParametersConverter) {
        this.parametersConverter = reportParametersConverter;
    }

    public void setFieldsConverter(ReportFieldsConverter reportFieldsConverter) {
        this.fieldsConverter = reportFieldsConverter;
    }

    public ReportParametersConverter getParametersConverter() {
        return parametersConverter;
    }

    public ReportFieldsConverter getFieldsConverter() {
        return fieldsConverter;
    }
}

