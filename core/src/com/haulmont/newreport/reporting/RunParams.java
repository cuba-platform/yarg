/**
 *
 * @author degtyarjov
 * @version $Id$
 */
package com.haulmont.newreport.reporting;

import com.haulmont.newreport.structure.Report;
import com.haulmont.newreport.structure.ReportTemplate;

import java.util.Collections;
import java.util.Map;

public class RunParams {
    protected Report report;
    protected String templateCode = ReportTemplate.DEFAULT_TEMPLATE_CODE;
    protected Map<String, Object> params = Collections.emptyMap();

    public RunParams(Report report) {
        this.report = report;
    }

    public RunParams templateCode(String templateCode) {
        this.templateCode = templateCode;
        return this;
    }

    public RunParams params(Map<String, Object> params) {
        this.params = params;
        return this;
    }
}
