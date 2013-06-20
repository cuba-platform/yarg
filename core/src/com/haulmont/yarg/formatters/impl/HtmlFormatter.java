/*
 * Copyright (c) 2011 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Artamonov Yuryi
 * Created: 18.03.11 13:11
 *
 * $Id: HtmlFormatter.java 7511 2012-04-05 13:26:17Z degtyarjov $
 */
package com.haulmont.yarg.formatters.impl;

import com.haulmont.yarg.structure.impl.BandData;
import com.haulmont.yarg.exception.ReportingException;
import com.haulmont.yarg.exception.UnsupportedFormatException;
import com.haulmont.yarg.structure.ReportOutputType;
import com.haulmont.yarg.structure.ReportTemplate;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Document formatter for '.html' and '.ftl' file types
 */
public class HtmlFormatter extends AbstractFormatter {
    public HtmlFormatter(BandData rootBand, ReportTemplate templateFile, OutputStream outputStream) {
        super(rootBand, templateFile, outputStream);
    }

    @Override
    public void renderDocument() {
        ReportOutputType outputType = reportTemplate.getOutputType();
        if (ReportOutputType.custom.equals(outputType) || ReportOutputType.csv.equals(outputType) || ReportOutputType.html.equals(outputType)) {
            writeHtmlDocument(rootBand, outputStream);
        } else if (ReportOutputType.pdf.equals(outputType)) {
            ByteArrayOutputStream htmlOutputStream = new ByteArrayOutputStream();
            writeHtmlDocument(rootBand, htmlOutputStream);

            String htmlContent = new String(htmlOutputStream.toByteArray());
            renderPdfDocument(htmlContent, outputStream);

        } else {
            throw new UnsupportedFormatException();
        }
    }


    private void renderPdfDocument(String htmlContent, OutputStream outputStream) {
        ITextRenderer renderer = new ITextRenderer();
        try {
            File tmpFile = File.createTempFile("htmlReport", ".htm");
            DataOutputStream dataOutputStream = new DataOutputStream(new FileOutputStream(tmpFile));
            dataOutputStream.write(htmlContent.getBytes(Charset.forName("UTF-8")));
            dataOutputStream.close();

            String url = tmpFile.toURI().toURL().toString();
            renderer.setDocument(url);

            renderer.layout();
            renderer.createPDF(outputStream);

            FileUtils.deleteQuietly(tmpFile);
        } catch (Exception e) {
            throw wrapWithReportingException("", e);
        }
    }

    private void writeHtmlDocument(BandData rootBand, OutputStream outputStream) {
        Map templateModel = getTemplateModel(rootBand);

        Template htmlTemplate = getTemplate();
        Writer htmlWriter = new OutputStreamWriter(outputStream);

        try {
            htmlTemplate.process(templateModel, htmlWriter);
            htmlWriter.close();
        } catch (TemplateException fmException) {
            throw wrapWithReportingException("FreeMarkerException: " + fmException.getMessage());
        } catch (ReportingException e) {
            throw e;
        } catch (Exception e) {
            throw wrapWithReportingException("An error occurred while rendering html document.", e);
        }
    }

    private Map getTemplateModel(BandData rootBand) {
        Map<String, Object> model = new HashMap<String, Object>();
        model.put(rootBand.getName(), getBandModel(rootBand));
        return model;
    }

    private Map getBandModel(BandData band) {
        Map<String, Object> model = new HashMap<String, Object>();

        Map<String, Object> bands = new HashMap<String, Object>();
        for (String bandName : band.getChildrenBands().keySet()) {
            List<BandData> subBands = band.getChildrenBands().get(bandName);
            List<Map> bandModels = new ArrayList<Map>();
            for (BandData child : subBands)
                bandModels.add(getBandModel(child));

            bands.put(bandName, bandModels);
        }
        model.put("bands", bands);

        model.put("fields", band.getData());

        return model;
    }

    private Template getTemplate() {
        try {
            String templateContent = IOUtils.toString(reportTemplate.getDocumentContent());
            StringTemplateLoader stringLoader = new StringTemplateLoader();
            stringLoader.putTemplate(reportTemplate.getDocumentName(), templateContent);

            Configuration fmConfiguration = new Configuration();
            fmConfiguration.setTemplateLoader(stringLoader);
            fmConfiguration.setDefaultEncoding("UTF-8");

            Template htmlTemplate = fmConfiguration.getTemplate(reportTemplate.getDocumentName());
            return htmlTemplate;
        } catch (Exception e) {
            throw wrapWithReportingException("An error occurred while creating freemarker template", e);
        }
    }
}
