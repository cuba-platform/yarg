/**
 *
 * @author degtyarjov
 * @version $Id$
 */
package com.haulmont.yarg.structure.impl;

import com.haulmont.yarg.formatters.CustomReport;
import com.haulmont.yarg.structure.ReportOutputType;
import com.haulmont.yarg.structure.ReportTemplate;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class ReportTemplateBuilder {
    private ReportTemplateImpl reportTemplate;


    public ReportTemplateBuilder() {
        reportTemplate = new ReportTemplateImpl();
    }

    public ReportTemplateBuilder code(String code) {
        reportTemplate.code = code;
        return this;
    }

    public ReportTemplateBuilder documentName(String documentName) {
        reportTemplate.documentName = documentName;
        return this;
    }

    public ReportTemplateBuilder documentPath(String documentPath) {
        reportTemplate.documentPath = documentPath;
        return this;
    }

    public ReportTemplateBuilder readFileFromPath() throws IOException {
        reportTemplate.documentContent = FileUtils.readFileToByteArray(new File(reportTemplate.documentPath));
        return this;
    }

    public ReportTemplateBuilder documentContent(byte[] documentContent) {
        reportTemplate.documentContent = documentContent;
        return this;
    }

    public ReportTemplateBuilder documentContent(InputStream documentContent) throws IOException {
        reportTemplate.documentContent = IOUtils.toByteArray(documentContent);
        return this;
    }

    public ReportTemplateBuilder outputType(ReportOutputType outputType) {
        reportTemplate.reportOutputType = outputType;
        return this;
    }

    public ReportTemplateBuilder outputNamePattern(String outputNamePattern) {
        reportTemplate.outputNamePattern = outputNamePattern;
        return this;
    }

    public ReportTemplateBuilder custom(CustomReport customReport) {
        reportTemplate.custom = true;
        reportTemplate.customReport = customReport;
        return this;
    }

    public ReportTemplate build() {
        return reportTemplate;
    }
}
