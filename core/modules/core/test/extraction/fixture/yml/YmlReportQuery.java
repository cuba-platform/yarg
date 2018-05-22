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

package extraction.fixture.yml;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.haulmont.yarg.structure.ReportQuery;

import java.util.Map;

import static com.haulmont.yarg.loaders.factory.DefaultLoaderFactory.*;

public class YmlReportQuery implements ReportQuery {

    protected String name;

    @JsonIgnore
    protected String loaderType;

    @JsonIgnore
    protected String script;

    @JsonProperty("params")
    protected Map<String, Object> additionalParams;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getScript() {
        return script;
    }

    @Override
    public String getLinkParameterName() {
        return null;
    }

    @Override
    public String getLoaderType() {
        return loaderType;
    }

    @Override
    public Boolean getProcessTemplate() {
        return false;
    }

    @Override
    public Map<String, Object> getAdditionalParams() {
        return additionalParams;
    }

    @JsonAnySetter
    public void set(String name, Object value) {
        switch (name) {
            case SQL_DATA_LOADER:
            case JSON_DATA_LOADER:
            case GROOVY_DATA_LOADER:
                loaderType = name;
                script = value.toString();
        }
    }

}
