package com.haulmont.newreport.structure.impl;

import com.google.common.base.Preconditions;
import com.haulmont.newreport.structure.*;

import java.util.ArrayList;
import java.util.HashMap;

public class ReportBuilder {
    private ReportImpl report;
    private BandDefinitionImpl rootBandDefinition;

    public ReportBuilder() {
        rootBandDefinition = new BandDefinitionImpl(Band.ROOT_BAND_NAME, null);
        report = new ReportImpl();
        report.rootBandDefinition = rootBandDefinition;
    }

    public ReportBuilder band(BandDefinition bandDefinition) {
        Preconditions.checkNotNull(bandDefinition, "\"bandDefinition\" parameter can not be null");
        BandDefinitionImpl wrapperBandDefinition = new BandDefinitionImpl(bandDefinition);
        rootBandDefinition.childrenBandDefinitions.add(wrapperBandDefinition);
        wrapperBandDefinition.parentBandDefinition = rootBandDefinition;
        return this;
    }

    public ReportBuilder template(ReportTemplate reportTemplate) {
        Preconditions.checkNotNull(reportTemplate, "\"reportTemplate\" parameter can not be null");
        report.reportTemplates.put(reportTemplate.getCode(), reportTemplate);
        return this;
    }

    public ReportBuilder parameter(ReportParameter reportParameter) {
        Preconditions.checkNotNull(reportParameter, "\"reportParameter\" parameter can not be null");
        report.reportParameters.add(reportParameter);
        return this;
    }

    public ReportBuilder valueFormat(ReportValueFormat reportValueFormat) {
        Preconditions.checkNotNull(reportValueFormat, "\"reportValueFormat\" parameter can not be null");
        report.reportValueFormats.add(reportValueFormat);
        return this;
    }

    public ReportBuilder name(String name) {
        Preconditions.checkNotNull(name, "\"name\" parameter can not be null");
        report.name = name;
        return this;
    }

    public Report build() {
        return report;
    }

}
