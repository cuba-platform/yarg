package com.haulmont.yarg.structure.impl;

import com.google.common.base.Preconditions;
import com.haulmont.yarg.structure.ReportBand;
import com.haulmont.yarg.structure.ReportQuery;

import java.util.ArrayList;

public class BandBuilder {
    ReportBandImpl bandDefinition = new ReportBandImpl(null, null, new ArrayList<ReportBand>(), new ArrayList<ReportQuery>(), BandOrientation.HORIZONTAL);

    public BandBuilder band(ReportBand bandDefinition) {
        Preconditions.checkNotNull(bandDefinition, "\"bandDefinition\" parameter can not be null");
        ReportBandImpl copyBand = new ReportBandImpl(bandDefinition);
        copyBand.parentBandDefinition = this.bandDefinition;
        this.bandDefinition.childrenBandDefinitions.add(copyBand);
        return this;
    }

    public BandBuilder dataSet(String name, String script, String loaderType) {
        Preconditions.checkNotNull(name, "\"name\" parameter can not be null");
        Preconditions.checkNotNull(script, "\"script\" parameter can not be null");
        Preconditions.checkNotNull(loaderType, "\"loaderType\" parameter can not be null");
        bandDefinition.reportQueries.add(new ReportQueryImpl(name, script, loaderType));
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
        return bandDefinition;
    }
}
