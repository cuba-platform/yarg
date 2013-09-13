package com.haulmont.yarg.loaders.impl;

import com.haulmont.yarg.exception.DataLoadingException;
import com.haulmont.yarg.structure.BandData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author degtyarjov
 * @version $Id: AbstractDbDataLoader.java 9328 2012-10-18 15:28:32Z artamonov $
 */
public abstract class AbstractDbDataLoader extends AbstractDataLoader {
    protected void addParentBandDataToParameters(BandData parentBand, Map<String, Object> currentParams) {
        if (parentBand != null) {
            String parentBandName = parentBand.getName();

            for (Map.Entry<String, Object> entry : parentBand.getData().entrySet()) {
                currentParams.put(parentBandName + "." + entry.getKey(), entry.getValue());
            }
        }
    }

    protected List<Map<String, Object>> fillOutputData(List resList, List<String> parametersNames) {
        List<Map<String, Object>> outputData = new ArrayList<Map<String, Object>>();

        for (Object resultRecordObject : resList) {
            Map<String, Object> outputParameters = new HashMap<String, Object>();
            if (resultRecordObject instanceof Object[]) {
                Object[] resultRecord = (Object[]) resultRecordObject;

                if (resultRecord.length != parametersNames.size()) {
                    throw new DataLoadingException(String.format("Result set size [%d] does not match output fields count [%s]. Detected output fields %s", resultRecord.length, parametersNames.size(), parametersNames));
                }

                for (Integer i = 0; i < resultRecord.length; i++) {
                    outputParameters.put(parametersNames.get(i), resultRecord[i]);
                }
            } else {
                outputParameters.put(parametersNames.get(0), resultRecordObject);
            }
            outputData.add(outputParameters);
        }
        return outputData;
    }

    protected static class QueryPack {
        private String query;
        private Object[] params;

        public QueryPack(String query, Object[] params) {
            this.query = query;
            this.params = params;
        }

        public String getQuery() {
            return query;
        }

        public Object[] getParams() {
            return params;
        }
    }
}