/**
 *
 * @author degtyarjov
 * @version $Id$
 */
package com.haulmont.yarg.structure.impl;

import com.google.common.base.Preconditions;
import com.haulmont.yarg.structure.ReportBand;
import com.haulmont.yarg.structure.ReportQuery;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ReportBandImpl implements ReportBand {
    protected String name;
    protected ReportBand parentBandDefinition;
    protected List<ReportBand> childrenBandDefinitions;
    protected List<ReportQuery> reportQueries;
    protected BandOrientation orientation;

    ReportBandImpl() {
        this.childrenBandDefinitions = new ArrayList<ReportBand>();
        this.reportQueries = new ArrayList<ReportQuery>();
        this.orientation = BandOrientation.HORIZONTAL;
    }

    public ReportBandImpl(String name, ReportBand parentBandDefinition, Collection<ReportBand> childrenBandDefinitions, Collection<ReportQuery> reportQueries, BandOrientation orientation) {
        this.name = name;
        this.parentBandDefinition = parentBandDefinition;
        this.childrenBandDefinitions = new ArrayList<ReportBand>(childrenBandDefinitions);
        this.reportQueries = new ArrayList<ReportQuery>(reportQueries);
        this.orientation = orientation;

        validate();
    }

    public ReportBandImpl(String name, ReportBand parentBandDefinition, BandOrientation orientation) {
        this(name, parentBandDefinition, new ArrayList<ReportBand>(), new ArrayList<ReportQuery>(), orientation);
    }

    public ReportBandImpl(String name, ReportBand parentBandDefinition) {
        this(name, parentBandDefinition, Collections.<ReportBand>emptyList(), Collections.<ReportQuery>emptyList(), BandOrientation.HORIZONTAL);
    }

    public ReportBandImpl(ReportBand instanceToCopy) {
        this(instanceToCopy.getName(), instanceToCopy.getParent(), instanceToCopy.getChildren(), instanceToCopy.getReportQueries(), instanceToCopy.getBandOrientation());
    }

    void validate() {
        Preconditions.checkNotNull(this.name, "\"name\" parameter can not be null");
        Preconditions.checkNotNull(this.orientation, "\"orientation\" parameter can not be null");
    }

    public String getName() {
        return name;
    }

    public ReportBand getParent() {
        return parentBandDefinition;
    }

    public List<ReportBand> getChildren() {
        return Collections.unmodifiableList(childrenBandDefinitions);
    }

    public List<ReportQuery> getReportQueries() {
        return Collections.unmodifiableList(reportQueries);
    }

    public BandOrientation getBandOrientation() {
        return orientation;
    }
}
