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

package com.haulmont.yarg.loaders.impl;

import com.haulmont.yarg.exception.DataLoadingException;
import com.haulmont.yarg.structure.BandData;
import groovy.text.GStringTemplateEngine;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author degtyarjov
 * @version $Id$
 */
public abstract class AbstractDbDataLoader extends AbstractDataLoader {

    public static final Pattern COMMON_PARAM_PATTERN = Pattern.compile("\\$\\{(.+?)\\}");

    protected List<Map<String, Object>> fillOutputData(List resList, List<OutputValue> parametersNames) {
        List<Map<String, Object>> outputData = new ArrayList<Map<String, Object>>();

        for (Object resultRecordObject : resList) {
            Map<String, Object> outputValues = new HashMap<String, Object>();
            if (resultRecordObject instanceof Object[]) {
                Object[] resultRecord = (Object[]) resultRecordObject;

                if (resultRecord.length != parametersNames.size()) {
                    throw new DataLoadingException(String.format("Result set size [%d] does not match output fields count [%s]. Detected output fields %s", resultRecord.length, parametersNames.size(), parametersNames));
                }

                for (Integer i = 0; i < resultRecord.length; i++) {
                    OutputValue outputValue = parametersNames.get(i);
                    Object value = resultRecord[i];
                    putValue(outputValues, outputValue, value);

                }
            } else {
                OutputValue outputValue = parametersNames.get(0);
                putValue(outputValues, outputValue, resultRecordObject);
            }
            outputData.add(outputValues);
        }
        return outputData;
    }

    private void putValue(Map<String, Object> outputData, OutputValue outputValue, Object value) {
        outputData.put(outputValue.getValueName(), value);
        if (StringUtils.isNotBlank(outputValue.getSynonym())) {
            outputData.put(outputValue.getSynonym(), value);
        }
    }

    protected QueryPack prepareQuery(String query, BandData parentBand, Map<String, Object> reportParams) {
        Map<String, Object> currentParams = new HashMap<String, Object>();
        if (reportParams != null) {
            currentParams.putAll(reportParams);
        }

        //adds parameters from parent bands hierarchy
        while (parentBand != null) {
            addParentBandDataToParameters(parentBand, currentParams);
            parentBand = parentBand.getParentBand();
        }

        List<QueryParameter> queryParameters = new ArrayList<QueryParameter>();
        HashSet<String> paramNames = findParameterNames(query);
        Map<String, String> paramsToRemoveFromQuery = new LinkedHashMap<String, String>();

        for (String paramName : paramNames) {
            Object paramValue = currentParams.get(paramName);
            String alias = "${" + paramName + "}";

            String paramNameRegexp = "\\$\\{" + paramName + "\\}";
            String valueRegexp = "([\\w|\\d|\\.|\\_]+|\'.+?\'|\".+?\"|\\(.+?\\))";//fieldName|literal|list or sub-query
            String andRegexp = "\\s+and\\s+";
            String orRegexp = "\\s+or\\s+";
            String operatorRegexp = "(=|>=|<=|\\slike\\s|>|<|\\sin\\s)";

            String expression1Rgxp = "\\s*" + valueRegexp + "\\s*" + operatorRegexp + "\\s*" + paramNameRegexp + "\\s*";
            String expression2Rgxp = "\\s*" + paramNameRegexp + "\\s*" + operatorRegexp + "\\s*" + valueRegexp + "\\s*";
            String expressionRgxp = "(" + expression1Rgxp + "|" + expression2Rgxp + ")";

            String andFirstRgxp = andRegexp + expressionRgxp;
            String orFirstRgxp = orRegexp + expressionRgxp;
            String andLastRgxp = expressionRgxp + andRegexp;
            String orLastRgxp = expressionRgxp + orRegexp;

            String isNullRgxp = paramNameRegexp + "\\s+is\\s+null";
            String isNotNullRgxp = paramNameRegexp + "\\s+is\\s+not\\s+null";

            String boundsRegexp = "\\[\\[.+?" + paramNameRegexp + ".+?\\]\\]";

            if (paramValue == null && reportParams != null && reportParams.containsKey(paramName)) {//if value == null && this is user parameter - remove condition from query
                paramsToRemoveFromQuery.put("(?i)" + andFirstRgxp, " and 1=1 ");
                paramsToRemoveFromQuery.put("(?i)" + andLastRgxp, " 1=1 and ");
                paramsToRemoveFromQuery.put("(?i)" + orFirstRgxp, " or 1=0 ");
                paramsToRemoveFromQuery.put("(?i)" + orLastRgxp, " 1=0 or ");

                paramsToRemoveFromQuery.put("(?i)" + expressionRgxp, " 1=1 ");
                paramsToRemoveFromQuery.put("(?i)" + isNullRgxp, " 1=1 ");
                paramsToRemoveFromQuery.put("(?i)" + isNotNullRgxp, " 1=0 ");

                paramsToRemoveFromQuery.put("(?i)" + boundsRegexp, " ");
            } else if (query.contains(alias)) {//otherwise - create parameter and save each entry's position
                Pattern pattern = Pattern.compile(paramNameRegexp);
                Matcher replaceMatcher = pattern.matcher(query);

                int subPosition = 0;
                while (replaceMatcher.find(subPosition)) {
                    subPosition = replaceMatcher.start();
                    queryParameters.add(new QueryParameter(paramNameRegexp, subPosition, convertParameter(paramValue)));
                    subPosition = replaceMatcher.end();
                }
            }
        }

        for (Map.Entry<String, String> entry : paramsToRemoveFromQuery.entrySet()) {
            query = query.replaceAll(entry.getKey(), entry.getValue());
        }
        query = query.replaceAll("\\[\\[", "");
        query = query.replaceAll("\\]\\]", "");

        // Sort params by position
        Collections.sort(queryParameters, new Comparator<QueryParameter>() {
            @Override
            public int compare(QueryParameter o1, QueryParameter o2) {
                return o1.getPosition().compareTo(o2.getPosition());
            }
        });

        //normalize params position to 1..n
        for (int i = 1; i <= queryParameters.size(); i++) {
            QueryParameter queryParameter = queryParameters.get(i - 1);
            queryParameter.setPosition(i);
        }

        for (QueryParameter parameter : queryParameters) {
            query = insertParameterToQuery(query, parameter);
        }

        return new QueryPack(query.trim().replaceAll(" +", " "), queryParameters.toArray(new QueryParameter[queryParameters.size()]));
    }

    @SuppressWarnings("unchecked")
    protected String processQueryTemplate(String query, BandData parentBand, Map<String, Object> reportParams) {
        try {
            GStringTemplateEngine engine = new GStringTemplateEngine();
            Map bindings = new HashMap();
            if (reportParams != null) {
                bindings.putAll(reportParams);
            }
            while (parentBand != null) {
                if (parentBand.getData() != null) {
                    bindings.put(parentBand.getName(), parentBand.getData());
                }
                parentBand = parentBand.getParentBand();
            }
            return engine.createTemplate(query).make(bindings).toString();
        } catch (ClassNotFoundException e) {
            throw new DataLoadingException(String.format("An error occurred while loading processing query template [%s]", query), e);
        } catch (IOException e) {
            throw new DataLoadingException(String.format("An error occurred while loading processing query template [%s]", query), e);
        }
    }

    protected HashSet<String> findParameterNames(String query) {
        HashSet<String> paramsStr = new LinkedHashSet<String>();
        Matcher paramMatcher = COMMON_PARAM_PATTERN.matcher(query);
        while (paramMatcher.find()) {
            String paramName = paramMatcher.group(1);
            paramsStr.add(paramName);
        }
        return paramsStr;
    }

    protected String insertParameterToQuery(String query, QueryParameter parameter) {
        if (parameter.isSingleValue()) {
            // Replace single parameter with ?
            query = query.replaceAll(parameter.getParamRegexp(), "?");
        } else {
            // Replace multiple parameter with (?,..(N)..,?)
            List<?> multipleValues = parameter.getMultipleValues();
            StringBuilder builder = new StringBuilder(" (");
            for (Object value : multipleValues) {
                builder.append("?,");
            }
            builder.deleteCharAt(builder.length() - 1);
            builder.append(")");

            query = query.replaceAll("\\(\\s*" + parameter.getParamRegexp() + "\\s*\\)", builder.toString());//if user already set up () - we remove it
            query = query.replaceAll(parameter.getParamRegexp(), builder.toString());
        }
        return query;
    }

    protected static class QueryPack {
        private String query;
        private QueryParameter[] params;

        public QueryPack(String query, QueryParameter[] params) {
            this.query = query;
            this.params = params;
        }

        public String getQuery() {
            return query;
        }

        public QueryParameter[] getParams() {
            return params;
        }
    }

    protected static class QueryParameter {
        private Integer position;
        private Object value;
        private String paramRegexp;

        public QueryParameter(String paramRegexp, Integer position, Object value) {
            this.position = position;
            this.value = value;
            this.paramRegexp = paramRegexp;
        }

        public void setPosition(Integer position) {
            this.position = position;
        }

        public Integer getPosition() {
            return position;
        }

        public Object getValue() {
            return value;
        }

        public String getParamRegexp() {
            return paramRegexp;
        }

        public boolean isSingleValue() {
            return !(value instanceof Collection || value instanceof Object[]);
        }

        public List<?> getMultipleValues() {
            if (isSingleValue()) {
                return Collections.singletonList(value);
            } else {
                if (value instanceof Collection) {
                    return new ArrayList<Object>((Collection<?>) value);
                } else if (value instanceof Object[]) {
                    return Arrays.asList((Object[]) value);
                }
            }

            return null;
        }
    }

    protected static class OutputValue {
        private String valueName;
        private String synonym;

        public OutputValue(String valueName) {
            this.valueName = valueName;
        }

        public void setSynonym(String synonym) {
            this.synonym = synonym;
        }

        public String getValueName() {
            return valueName;
        }

        public String getSynonym() {
            return synonym;
        }
    }
}