package com.haulmont.newreport.structure.impl;

import com.haulmont.newreport.structure.BandDefinition;
import com.haulmont.newreport.structure.Report;
import com.haulmont.newreport.structure.ReportParameter;
import com.haulmont.newreport.structure.ReportTemplate;

import java.util.ArrayList;
import java.util.HashMap;

public class ReportBuilder {
    private ReportImpl report;

    public ReportBuilder() {
        BandDefinitionImpl rootBandDefinition = new BandDefinitionImpl(Band.ROOT_BAND_NAME, null);
        report = new ReportImpl(null, new HashMap<String, ReportTemplate>(), rootBandDefinition, new ArrayList<ReportParameter>());
    }

    public ReportBuilder band(BandDefinition bandDefinition) {
        BandDefinitionImpl wrapperBandDefinition = new BandDefinitionImpl(bandDefinition);
        report.getRootBandDefinition().getChildrenBandDefinitions().add(wrapperBandDefinition);
        wrapperBandDefinition.setParentBandDefinition(report.getRootBandDefinition());
        return this;
    }

    public ReportBuilder template(ReportTemplate reportTemplate) {
        report.getReportTemplates().put(reportTemplate.getCode(), reportTemplate);
        return this;
    }

    public ReportBuilder parameter(ReportParameter reportParameter) {
        report.getReportParameters().add(reportParameter);
        return this;
    }

    public ReportBuilder name(String name) {
        report.setName(name);
        return this;
    }

    public Report build() {
        return report;
    }

}
