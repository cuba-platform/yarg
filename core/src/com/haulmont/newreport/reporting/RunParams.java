/**
 *
 * @author degtyarjov
 * @version $Id$
 */
package com.haulmont.newreport.reporting;

import com.google.common.base.Preconditions;
import com.haulmont.newreport.structure.Report;
import com.haulmont.newreport.structure.ReportTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class RunParams {
    protected Report report;
    protected ReportTemplate reportTemplate;
    protected Map<String, Object> params = new HashMap<String, Object>();

    public RunParams(Report report) {
        this.report = report;
        this.reportTemplate = report.getReportTemplates().get(ReportTemplate.DEFAULT_TEMPLATE_CODE);
    }

    public RunParams templateCode(String templateCode) {
        Preconditions.checkNotNull(templateCode, "\"templateCode\" parameter can not be null");
        this.reportTemplate = report.getReportTemplates().get(templateCode);
        return this;
    }

    public RunParams template(ReportTemplate reportTemplate) {
        Preconditions.checkNotNull(reportTemplate, "\"reportTemplate\" parameter can not be null");
        this.reportTemplate = reportTemplate;
        return this;
    }

    public RunParams params(Map<String, Object> params) {
        Preconditions.checkNotNull(params, "\"params\" parameter can not be null");
        this.params.putAll(params);
        return this;
    }

    public RunParams param(String key, Object value){
        params.put(key, value);
        return this;
    }
}
