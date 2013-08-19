package com.haulmont.yarg.structure.impl;

import com.google.common.base.Preconditions;
import com.haulmont.yarg.structure.ReportBand;
import com.haulmont.yarg.structure.ReportQuery;

import java.util.ArrayList;

public class BandBuilder {
    ReportBandImpl bandDefinition = new ReportBandImpl();

    public BandBuilder child(ReportBand bandDefinition) {
        Preconditions.checkNotNull(bandDefinition, "\"bandDefinition\" parameter can not be null");
        ReportBandImpl copyBand = new ReportBandImpl(bandDefinition);
        copyBand.parentBandDefinition = this.bandDefinition;
        this.bandDefinition.childrenBandDefinitions.add(copyBand);
        return this;
    }

    public BandBuilder query(String name, String script, String loaderType) {
        bandDefinition.reportQueries.add(new ReportQueryImpl(name, script, loaderType, null, null));
        return this;
    }

    public BandBuilder query(String name, String script, String loaderType, String linkParameterName) {
        bandDefinition.reportQueries.add(new ReportQueryImpl(name, script, loaderType, linkParameterName, null));
        return this;
    }

    public BandBuilder name(String name) {
        Preconditions.checkNotNull(name, "\"name\" parameter can not be null");
        bandDefinition.name = name;
        return this;

    }

    public BandBuilder orientation(BandOrientation orientation) {
        Preconditions.checkNotNull(orientation, "\"orientation\" parameter can not be null");
        bandDefinition.orientation = orientation;
        return this;

    }

    public ReportBand build() {
        bandDefinition.validate();
        return bandDefinition;
    }
}
