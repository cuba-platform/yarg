/**
 *
 * @author degtyarjov
 * @version $Id$
 */
package com.haulmont.yarg.reporting;

import com.haulmont.yarg.structure.Report;
import com.haulmont.yarg.structure.ReportOutputType;

import java.io.Serializable;

public class ReportOutputDocument implements Serializable {
    protected Report report;
    protected byte[] content;
    protected String documentName;
    protected ReportOutputType reportOutputType;

    public ReportOutputDocument(Report report, byte[] content, String documentName, ReportOutputType reportOutputType) {
        this.report = report;
        this.content = content;
        this.documentName = documentName;
        this.reportOutputType = reportOutputType;
    }

    public void setReport(Report report) {
        this.report = report;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public void setDocumentName(String documentName) {
        this.documentName = documentName;
    }

    public void setReportOutputType(ReportOutputType reportOutputType) {
        this.reportOutputType = reportOutputType;
    }

    public Report getReport() {
        return report;
    }

    public byte[] getContent() {
        return content;
    }

    public String getDocumentName() {
        return documentName;
    }

    public ReportOutputType getReportOutputType() {
        return reportOutputType;
    }
}
