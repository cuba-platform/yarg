package com.haulmont.yarg.structure.impl;

import com.google.common.base.Preconditions;
import com.haulmont.yarg.structure.*;

public class ReportBuilder {
    private ReportImpl report;
    private ReportBandImpl rootBandDefinition;

    public ReportBuilder() {
        rootBandDefinition = new ReportBandImpl(BandData.ROOT_BAND_NAME, null);
        report = new ReportImpl();
        report.rootBandDefinition = rootBandDefinition;
    }

    public ReportBuilder band(ReportBand bandDefinition) {
        Preconditions.checkNotNull(bandDefinition, "\"bandDefinition\" parameter can not be null");
        ReportBandImpl wrapperBandDefinition = new ReportBandImpl(bandDefinition);
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

    public ReportBuilder valueFormat(ReportFieldFormat reportFieldFormat) {
        Preconditions.checkNotNull(reportFieldFormat, "\"reportFieldFormat\" parameter can not be null");
        report.reportFieldFormats.add(reportFieldFormat);
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
