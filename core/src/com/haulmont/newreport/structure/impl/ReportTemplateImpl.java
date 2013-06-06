/**
 *
 * @author degtyarjov
 * @version $Id$
 */
package com.haulmont.newreport.structure.impl;

import com.haulmont.newreport.structure.ReportOutputType;
import com.haulmont.newreport.structure.ReportTemplate;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.*;

public class ReportTemplateImpl implements ReportTemplate {
    protected String code;
    protected String documentName;
    protected String documentPath;
    protected byte[] documentContent;
    protected ReportOutputType reportOutputType;
    protected String outputNamePattern;

    public ReportTemplateImpl(String code, String documentName, String documentPath, ReportOutputType reportOutputType, String outputNamePattern) throws IOException {
        this.code = code;
        this.documentName = documentName;
        this.documentPath = documentPath;
        this.documentContent = FileUtils.readFileToByteArray(new File(documentPath));
        this.reportOutputType = reportOutputType;
        this.outputNamePattern = outputNamePattern;
    }

    public ReportTemplateImpl(String code, String documentName, String documentPath, InputStream documentContent, ReportOutputType reportOutputType, String outputNamePattern) throws IOException {
        this.code = code;
        this.documentName = documentName;
        this.documentPath = documentPath;
        this.documentContent = IOUtils.toByteArray(documentContent);
        this.reportOutputType = reportOutputType;
        this.outputNamePattern = outputNamePattern;
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
}
