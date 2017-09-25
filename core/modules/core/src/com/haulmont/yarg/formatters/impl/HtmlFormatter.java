/*
 * Copyright 2013 Haulmont
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.haulmont.yarg.formatters.impl;

import com.haulmont.yarg.exception.ReportingException;
import com.haulmont.yarg.exception.UnsupportedFormatException;
import com.haulmont.yarg.formatters.factory.FormatterFactoryInput;
import com.haulmont.yarg.structure.BandData;
import com.haulmont.yarg.structure.ReportOutputType;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.BaseFont;
import freemarker.cache.StringTemplateLoader;
import freemarker.ext.beans.BeansWrapper;
import freemarker.ext.beans.MapModel;
import freemarker.template.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;

/**
 * Document formatter for '.html' and '.ftl' file types
 */
public class HtmlFormatter extends AbstractFormatter {
    protected BeansWrapper beansWrapper = new BeansWrapper();
    protected ObjectWrapper objectWrapper;
    protected String fontsDirectory;

    public HtmlFormatter(FormatterFactoryInput formatterFactoryInput) {
        super(formatterFactoryInput);
        supportedOutputTypes.add(ReportOutputType.custom);
        supportedOutputTypes.add(ReportOutputType.csv);
        supportedOutputTypes.add(ReportOutputType.html);
        supportedOutputTypes.add(ReportOutputType.pdf);
        beansWrapper.setNullModel(TemplateScalarModel.EMPTY_STRING);

        objectWrapper = new DefaultObjectWrapper() {
            @Override
            public TemplateModel wrap(Object obj) throws TemplateModelException {
                if (obj instanceof Map) {
                    return new MapModel((Map) obj, beansWrapper);
                }
                return super.wrap(obj);
            }
        };
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

    public void setFontsDirectory(String fontsDirectory) {
        this.fontsDirectory = fontsDirectory;
    }

    protected void renderPdfDocument(String htmlContent, OutputStream outputStream) {
        ITextRenderer renderer = new ITextRenderer();
        File temporaryFile = null;
        try {
            temporaryFile = File.createTempFile("htmlReport", ".htm");
            DataOutputStream dataOutputStream = new DataOutputStream(new FileOutputStream(temporaryFile));
            dataOutputStream.write(htmlContent.getBytes(Charset.forName("UTF-8")));
            dataOutputStream.close();

            loadFonts(renderer);

            String url = temporaryFile.toURI().toURL().toString();
            renderer.setDocument(url);

            renderer.layout();
            renderer.createPDF(outputStream);
        } catch (Exception e) {
            throw wrapWithReportingException("", e);
        } finally {
            FileUtils.deleteQuietly(temporaryFile);
        }
    }

    protected void loadFonts(ITextRenderer renderer) {
        if (StringUtils.isNotBlank(fontsDirectory)) {
            File systemFontsDir = new File(fontsDirectory);
            loadFontsFromDirectory(renderer, systemFontsDir);
        }
    }

    protected void loadFontsFromDirectory(ITextRenderer renderer, File fontsDir) {
        if (fontsDir.exists()) {
            if (fontsDir.isDirectory()) {
                File[] files = fontsDir.listFiles((dir, name) -> {
                    String lower = name.toLowerCase();
                    return lower.endsWith(".otf") || lower.endsWith(".ttf");
                });
                for (File file : files) {
                    try {
                        // Usage of some fonts may be not permitted
                        renderer.getFontResolver().addFont(file.getAbsolutePath(), BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
                    } catch (IOException | DocumentException e) {
                        if (StringUtils.contains(e.getMessage(), "cannot be embedded due to licensing restrictions")) {
                            e.printStackTrace();//todo log message
                        } else {
                            e.printStackTrace();//todo log message
                        }
                    }
                }
            } else {
                //todo log message
            }
        } else {
            //todo log message
        }
    }

    protected void writeHtmlDocument(BandData rootBand, OutputStream outputStream) {
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

    protected Map getTemplateModel(BandData rootBand) {
        Map<String, Object> model = new HashMap<String, Object>();
        model.put(rootBand.getName(), getBandModel(rootBand));
        return model;
    }

    protected Map getBandModel(BandData band) {
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

    protected Template getTemplate() {
        try {
            String templateContent = IOUtils.toString(reportTemplate.getDocumentContent(), StandardCharsets.UTF_8);
            StringTemplateLoader stringLoader = new StringTemplateLoader();
            stringLoader.putTemplate(reportTemplate.getDocumentName(), templateContent);

            Configuration fmConfiguration = new Configuration();
            fmConfiguration.setTemplateLoader(stringLoader);
            fmConfiguration.setDefaultEncoding("UTF-8");

            Template htmlTemplate = fmConfiguration.getTemplate(reportTemplate.getDocumentName());
            htmlTemplate.setObjectWrapper(objectWrapper);
            return htmlTemplate;
        } catch (Exception e) {
            throw wrapWithReportingException("An error occurred while creating freemarker template", e);
        }
    }
}
