/**
 *
 * @author degtyarjov
 * @version $Id$
 */
package com.haulmont.yarg.reporting;

import com.google.common.base.Preconditions;
import com.haulmont.yarg.structure.Report;
import com.haulmont.yarg.structure.ReportTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Describes parameters necessary for report run
 */
public class RunParams {
    protected Report report;
    protected ReportTemplate reportTemplate;
    protected Map<String, Object> params = new HashMap<String, Object>();

    public RunParams(Report report) {
        this.report = report;
        this.reportTemplate = report.getReportTemplates().get(ReportTemplate.DEFAULT_TEMPLATE_CODE);
    }

    /**
     * Setup necessary template by string code. Throws validation exception if code is null or template not found
     * @param templateCode - string code of template
     */
    public RunParams templateCode(String templateCode) {
        Preconditions.checkNotNull(templateCode, "\"templateCode\" parameter can not be null");
        this.reportTemplate = report.getReportTemplates().get(templateCode);
        Preconditions.checkNotNull(reportTemplate, String.format("Report template not found for code [%s]", templateCode));
        return this;
    }

    /**
     * Setup template. Throws validation exception if template is null
     */
    public RunParams template(ReportTemplate reportTemplate) {
        Preconditions.checkNotNull(reportTemplate, "\"reportTemplate\" parameter can not be null");
        this.reportTemplate = reportTemplate;
        return this;
    }

    /**
     * Adds parameters from map
     */
    public RunParams params(Map<String, Object> params) {
        Preconditions.checkNotNull(params, "\"params\" parameter can not be null");
        this.params.putAll(params);
        return this;
    }

    /**
     * Add single parameter
     */
    public RunParams param(String key, Object value) {
        params.put(key, value);
        return this;
    }
}
