/**
 *
 * @author degtyarjov
 * @version $Id$
 */
package com.haulmont.newreport.structure.impl;

import com.haulmont.newreport.structure.BandDefinition;
import com.haulmont.newreport.structure.Report;
import com.haulmont.newreport.structure.ReportParameter;
import com.haulmont.newreport.structure.ReportTemplate;

import java.util.Collection;
import java.util.Map;

public class ReportImpl implements Report {

    protected String name;
    protected Map<String, ReportTemplate> reportTemplates;
    protected BandDefinition rootBandDefinition;
    protected Collection<ReportParameter> reportParameters;

    public ReportImpl(String name, Map<String, ReportTemplate> reportTemplates, BandDefinition rootBandDefinition, Collection<ReportParameter> reportParameters) {
        this.name = name;
        this.reportTemplates = reportTemplates;
        this.rootBandDefinition = rootBandDefinition;
        this.reportParameters = reportParameters;
    }

    void setName(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Map<String, ReportTemplate> getReportTemplates() {
        return reportTemplates;
    }

    @Override
    public BandDefinition getRootBandDefinition() {
        return rootBandDefinition;
    }

    @Override
    public Collection<ReportParameter> getReportParameters() {
        return reportParameters;
    }
}
