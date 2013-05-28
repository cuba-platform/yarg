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

import java.util.*;

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

    public ReportImpl(String name, BandDefinition rootBandDefinition) {
        this.name = name;
        this.reportTemplates = new HashMap<String, ReportTemplate>();
        this.rootBandDefinition = rootBandDefinition;
        this.reportParameters = new ArrayList<ReportParameter>();
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
        return Collections.unmodifiableMap(reportTemplates);
    }

    @Override
    public BandDefinition getRootBandDefinition() {
        return rootBandDefinition;
    }

    @Override
    public Collection<ReportParameter> getReportParameters() {
        return Collections.unmodifiableCollection(reportParameters);
    }
}
