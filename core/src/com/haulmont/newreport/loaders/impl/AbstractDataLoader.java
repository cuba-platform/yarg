/**
 *
 * @author degtyarjov
 * @version $Id$
 */
package com.haulmont.newreport.loaders.impl;

import com.haulmont.newreport.loaders.DataLoader;
import com.haulmont.newreport.loaders.ParametersConverter;
import com.haulmont.newreport.loaders.ResultsConverter;

public abstract class AbstractDataLoader implements DataLoader {
    protected ParametersConverter parametersConverter = null;
    protected ResultsConverter resultsConverter = null;


    protected <T> T convertParameter(Object input) {
        if (parametersConverter != null) {
            return parametersConverter.convert(input);
        } else {
            return (T) input;
        }
    }

    protected <T> T convertOutputValue(Object input) {
        if (resultsConverter != null) {
            return resultsConverter.convert(input);
        } else {
            return (T) input;
        }
    }

    public void setParametersConverter(ParametersConverter parametersConverter) {
        this.parametersConverter = parametersConverter;
    }

    public void setResultsConverter(ResultsConverter resultsConverter) {
        this.resultsConverter = resultsConverter;
    }

    public ParametersConverter getParametersConverter() {
        return parametersConverter;
    }

    public ResultsConverter getResultsConverter() {
        return resultsConverter;
    }
}

