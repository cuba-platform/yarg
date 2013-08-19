/**
 *
 * @author degtyarjov
 * @version $Id$
 */
package com.haulmont.yarg.structure.impl;

import com.google.common.base.Preconditions;
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
        Preconditions.checkNotNull(code, "\"code\" parameter can not be null");
        reportTemplate.code = code;
        return this;
    }

    public ReportTemplateBuilder documentName(String documentName) {
        Preconditions.checkNotNull(documentName, "\"documentName\" parameter can not be null");
        reportTemplate.documentName = documentName;
        return this;
    }

    public ReportTemplateBuilder documentPath(String documentPath) {
        reportTemplate.documentPath = documentPath;
        return this;
    }

    public ReportTemplateBuilder readFileFromPath() throws IOException {
        Preconditions.checkNotNull(reportTemplate.documentPath, "\"documentPath\" parameter is null. Can not load data from null path");
        reportTemplate.documentContent = FileUtils.readFileToByteArray(new File(reportTemplate.documentPath));
        return this;
    }

    public ReportTemplateBuilder documentContent(byte[] documentContent) {
        Preconditions.checkNotNull(documentContent, "\"documentContent\" parameter can not be null");
        reportTemplate.documentContent = documentContent;
        return this;
    }

    public ReportTemplateBuilder documentContent(InputStream documentContent) throws IOException {
        Preconditions.checkNotNull(documentContent, "\"documentContent\" parameter can not be null");
        reportTemplate.documentContent = IOUtils.toByteArray(documentContent);
        return this;
    }

    public ReportTemplateBuilder outputType(ReportOutputType outputType) {
        Preconditions.checkNotNull(outputType, "\"outputType\" parameter can not be null");
        reportTemplate.reportOutputType = outputType;
        return this;
    }

    public ReportTemplateBuilder outputNamePattern(String outputNamePattern) {
        reportTemplate.outputNamePattern = outputNamePattern;
        return this;
    }

    public ReportTemplateBuilder custom(CustomReport customReport) {
        Preconditions.checkNotNull(customReport, "\"customReport\" parameter can not be null");
        reportTemplate.custom = true;
        reportTemplate.customReport = customReport;
        return this;
    }

    public ReportTemplate build() {
        reportTemplate.validate();
        return reportTemplate;
    }
}
