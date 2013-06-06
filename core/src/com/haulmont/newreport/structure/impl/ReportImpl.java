/**
 *
 * @author degtyarjov
 * @version $Id$
 */
package com.haulmont.newreport.structure.impl;

import com.google.common.base.Preconditions;
import com.haulmont.newreport.structure.*;

import java.util.*;

public class ReportImpl implements Report {
    protected String name;
    protected Map<String, ReportTemplate> reportTemplates;
    protected BandDefinition rootBandDefinition;
    protected List<ReportParameter> reportParameters;
    protected List<ReportValueFormat> reportValueFormats;

    public ReportImpl(String name, Map<String, ReportTemplate> reportTemplates, BandDefinition rootBandDefinition, List<ReportParameter> reportParameters, List<ReportValueFormat> reportValueFormats) {
        Preconditions.checkNotNull(name, "\"name\" parameter can not be null");
        Preconditions.checkNotNull(rootBandDefinition, "\"rootBandDefinition\" parameter can not be null");
        Preconditions.checkNotNull(reportTemplates, "\"reportTemplates\" parameter can not be null");
        Preconditions.checkNotNull(reportParameters, "\"reportParameters\" parameter can not be null");
        Preconditions.checkNotNull(reportValueFormats, "\"reportValueFormats\" parameter can not be null");

        this.name = name;
        this.reportTemplates = reportTemplates;
        this.rootBandDefinition = rootBandDefinition;
        this.reportParameters = reportParameters;
        this.reportValueFormats = reportValueFormats;
    }

    ReportImpl() {
        this.name = "";
        this.rootBandDefinition = null;
        this.reportTemplates = new HashMap<String, ReportTemplate>();
        this.reportParameters = new ArrayList<ReportParameter>();
        this.reportValueFormats = new ArrayList<ReportValueFormat>();
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
    public List<ReportParameter> getReportParameters() {
        return Collections.unmodifiableList(reportParameters);
    }

    @Override
    public List<ReportValueFormat> getReportValueFormats() {
        return Collections.unmodifiableList(reportValueFormats);
    }
}
