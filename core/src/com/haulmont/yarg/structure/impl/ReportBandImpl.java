/**
 *
 * @author degtyarjov
 * @version $Id$
 */
package com.haulmont.yarg.structure.impl;

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

    public ReportBandImpl(String name, ReportBand parentBandDefinition, Collection<ReportBand> childrenBandDefinitions, Collection<ReportQuery> reportQueries, BandOrientation orientation) {
        this.name = name;
        this.parentBandDefinition = parentBandDefinition;
        this.childrenBandDefinitions = new ArrayList<ReportBand>(childrenBandDefinitions);
        this.reportQueries = new ArrayList<ReportQuery>(reportQueries);
        this.orientation = orientation;
    }

    public ReportBandImpl(String name, ReportBand parentBandDefinition, BandOrientation orientation) {
        this.name = name;
        this.parentBandDefinition = parentBandDefinition;
        this.orientation = orientation;
        childrenBandDefinitions = new ArrayList<ReportBand>();
        reportQueries = new ArrayList<ReportQuery>();
    }

    public ReportBandImpl(String name, ReportBand parentBandDefinition) {
        this.name = name;
        this.parentBandDefinition = parentBandDefinition;
        this.orientation = BandOrientation.HORIZONTAL;
        childrenBandDefinitions = new ArrayList<ReportBand>();
        reportQueries = new ArrayList<ReportQuery>();
    }

    public ReportBandImpl(ReportBand instanceToCopy) {
        name = instanceToCopy.getName();
        parentBandDefinition = instanceToCopy.getParent();
        childrenBandDefinitions = new ArrayList<ReportBand>(instanceToCopy.getChildren());
        reportQueries = new ArrayList<ReportQuery>(instanceToCopy.getReportQueries());
        orientation = instanceToCopy.getBandOrientation();
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
