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

import com.google.gson.*;
import com.haulmont.yarg.exception.DataLoadingException;
import com.haulmont.yarg.loaders.ReportDataLoader;
import com.haulmont.yarg.structure.BandData;
import com.haulmont.yarg.structure.ReportQuery;

import java.util.*;

public class JsonDataLoader implements ReportDataLoader {
    @Override
    public List<Map<String, Object>> loadData(ReportQuery reportQuery, BandData parentBand, Map<String, Object> params) {
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        Object parameterValue = params.get(reportQuery.getScript());
        if (parameterValue != null) {
            JsonElement json;
            if (parameterValue instanceof String) {
                JsonParser parser = new JsonParser();
                json = parser.parse((String) parameterValue);
            } else if (parameterValue instanceof JsonElement) {
                json = (JsonElement) parameterValue;
            } else {
                throw new DataLoadingException(
                        String.format("The parameter [%s] has type [%s]. Supported types [java.lang.String, com.google.gson.JsonElement]",
                                reportQuery.getScript(),
                                parameterValue.getClass()));
            }

            if (json instanceof JsonArray) {
                JsonArray jsonArray = (JsonArray) json;
                for (JsonElement next : jsonArray) {
                    if (next instanceof JsonObject) {
                        Map<String, Object> map = convertJsonToMap((JsonObject) next);
                        result.add(map);
                    }
                }
            } else if (json instanceof JsonObject) {
                Map<String, Object> map = convertJsonToMap((JsonObject) json);
                result.add(map);
            } else {
                throw new DataLoadingException(
                        String.format("Provided json is neither array nor object [%s]", json.toString()));
            }
        }

        return result;
    }

    private Map<String, Object> convertJsonToMap(JsonObject jsonObject) {
        Map<String, Object> map = new HashMap<String, Object>();
        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            if (entry.getValue() instanceof JsonPrimitive) {
                JsonPrimitive value = (JsonPrimitive) entry.getValue();
                map.put(entry.getKey(), value.getAsString());
            }
        }

        return map;
    }
}
