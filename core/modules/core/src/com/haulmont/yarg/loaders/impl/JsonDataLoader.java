/*
 * Copyright 2014 Haulmont
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
package com.haulmont.yarg.loaders.impl;

import com.haulmont.yarg.exception.DataLoadingException;
import com.haulmont.yarg.loaders.ReportDataLoader;
import com.haulmont.yarg.loaders.impl.json.JsonMap;
import com.haulmont.yarg.structure.BandData;
import com.haulmont.yarg.structure.ReportQuery;
import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONObject;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonDataLoader implements ReportDataLoader {
    protected Pattern parameterPattern = Pattern.compile("parameter=([A-z0-9_]+)");

    @Override
    public List<Map<String, Object>> loadData(ReportQuery reportQuery, BandData parentBand, Map<String, Object> params) {
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();


        Matcher matcher = parameterPattern.matcher(reportQuery.getScript());
        String parameterName = getParameterName(matcher);

        if (parameterName != null) {
            Object parameterValue = params.get(parameterName);
            if (parameterValue != null && StringUtils.isNotBlank(parameterValue.toString())) {
                String json = parameterValue.toString();
                String script = matcher.replaceAll("");

                if (StringUtils.isBlank(script)) {
                    throw new DataLoadingException(
                            String.format("The script doesn't contain json path expression. " +
                                    "Script [%s]", reportQuery.getScript()));
                }

                try {
                    Object scriptResult = JsonPath.read(json, script);
                    parseScriptResult(result, script, scriptResult);
                } catch (com.jayway.jsonpath.PathNotFoundException e) {
                    return Collections.emptyList();
                } catch (Throwable e) {
                    throw new DataLoadingException(
                            String.format("An error occurred while loading data with script [%s]", reportQuery.getScript()), e);
                }
            } else {
                return Collections.emptyList();
            }
        } else {
            throw new DataLoadingException(String.format("Query string doesn't contain link to parameter. " +
                    "Script [%s]", reportQuery.getScript()));
        }

        return result;
    }

    protected void parseScriptResult(List<Map<String, Object>> result, String script, Object scriptResult) {
        if (scriptResult instanceof List) {//JSONArray is also list
            List theList = (List) scriptResult;
            if (CollectionUtils.isNotEmpty(theList)) {
                Object listObject = theList.get(0);
                if (listObject instanceof JSONObject) {
                    for (Object object : theList) {
                        result.add(createMap((JSONObject) object));
                    }
                } else {
                    throw new DataLoadingException(
                            String.format("The list collected with script does not contain objects. " +
                                    "It contains %s instead. " +
                                    "Script [%s]", listObject, script));
                }
            }
        } else if (scriptResult instanceof JSONObject) {
            result.add(createMap((JSONObject) scriptResult));
        } else {
            throw new DataLoadingException(
                    String.format("The script collects neither object nor list of objects. " +
                            "Script [%s]", script));
        }
    }

    protected String getParameterName(Matcher matcher) {
        if (matcher.find()) {
            return matcher.group(1);
        }

        return null;
    }

    protected Map<String, Object> createMap(JSONObject jsonObject) {
        return new JsonMap(jsonObject);
    }
}
