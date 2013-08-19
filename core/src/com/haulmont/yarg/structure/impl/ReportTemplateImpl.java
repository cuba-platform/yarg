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

import java.io.*;

public class ReportTemplateImpl implements ReportTemplate {
    protected String code = ReportTemplate.DEFAULT_TEMPLATE_CODE;
    protected String documentName;
    protected String documentPath;
    protected byte[] documentContent;
    protected ReportOutputType reportOutputType;
    protected String outputNamePattern;

    protected CustomReport customReport;
    protected boolean custom = false;

    ReportTemplateImpl() {
    }

    public ReportTemplateImpl(String code, String documentName, String documentPath, InputStream documentContent, ReportOutputType reportOutputType) throws IOException {
        this.code = code;
        this.documentName = documentName;
        this.documentPath = documentPath;
        this.documentContent = IOUtils.toByteArray(documentContent);
        this.reportOutputType = reportOutputType;
    }

    public ReportTemplateImpl(String code, String documentName, String documentPath, ReportOutputType reportOutputType) throws IOException {
        this(code, documentName, documentPath, FileUtils.openInputStream(new File(documentPath)), reportOutputType);

        validate();
    }

    void validate() {
        if (!isCustom()) {
            Preconditions.checkNotNull(this.code, "\"code\" parameter can not be null");
            Preconditions.checkNotNull(this.documentName, "\"documentName\" parameter can not be null");
            Preconditions.checkNotNull(this.documentPath, "\"documentPath\" parameter can not be null");
            Preconditions.checkNotNull(this.reportOutputType, "\"reportOutputType\" parameter can not be null");
            Preconditions.checkNotNull(this.documentContent, "\"documentContent\" can not be null");
        }
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getDocumentName() {
        return documentName;
    }

    @Override
    public InputStream getDocumentContent() {
        return new ByteArrayInputStream(documentContent);
    }

    @Override
    public ReportOutputType getOutputType() {
        return reportOutputType;
    }

    public String getDocumentPath() {
        return documentPath;
    }

    public String getOutputNamePattern() {
        return outputNamePattern;
    }

    @Override
    public boolean isCustom() {
        return custom;
    }

    @Override
    public CustomReport getCustomReport() {
        return customReport;
    }
}
