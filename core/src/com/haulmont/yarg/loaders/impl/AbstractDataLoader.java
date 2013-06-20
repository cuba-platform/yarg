/**
 *
 * @author degtyarjov
 * @version $Id$
 */
package com.haulmont.yarg.loaders.impl;

import com.haulmont.yarg.loaders.FieldsConverter;
import com.haulmont.yarg.loaders.ReportDataLoader;
import com.haulmont.yarg.loaders.ParametersConverter;

public abstract class AbstractDataLoader implements ReportDataLoader {
    protected ParametersConverter parametersConverter = null;
    protected FieldsConverter fieldsConverter = null;


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

    public void setParametersConverter(ParametersConverter parametersConverter) {
        this.parametersConverter = parametersConverter;
    }

    public void setFieldsConverter(FieldsConverter fieldsConverter) {
        this.fieldsConverter = fieldsConverter;
    }

    public ParametersConverter getParametersConverter() {
        return parametersConverter;
    }

    public FieldsConverter getFieldsConverter() {
        return fieldsConverter;
    }
}

