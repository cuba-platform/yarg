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
