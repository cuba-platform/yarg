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

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.haulmont.yarg.structure.BandOrientation;
import com.haulmont.yarg.structure.ReportBand;
import com.haulmont.yarg.structure.ReportQuery;

import java.util.List;

public class YmlReportBand implements ReportBand {

    protected String name;
    @JsonDeserialize(using = BandOrientationDeserializer.class)
    protected BandOrientation orientation;
    @JsonBackReference("parent")
    protected YmlReportBand parent;
    @JsonManagedReference("parent")
    protected List<ReportBand> children;
    @JsonProperty("queries")
    protected List<ReportQuery> reportQueries;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public ReportBand getParent() {
        return parent;
    }

    @Override
    @JsonDeserialize(contentAs = YmlReportBand.class)
    public List<ReportBand> getChildren() {
        return children;
    }

    @Override
    @JsonDeserialize(contentAs = YmlReportQuery.class)
    public List<ReportQuery> getReportQueries() {
        return reportQueries;
    }

    @Override
    public BandOrientation getBandOrientation() {
        return orientation;
    }
}
