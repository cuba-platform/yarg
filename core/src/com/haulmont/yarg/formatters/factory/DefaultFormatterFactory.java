/**
 *
 * @author degtyarjov
 * @version $Id$
 */
package com.haulmont.yarg.formatters.factory;

import com.haulmont.yarg.exception.UnsupportedFormatException;
import com.haulmont.yarg.formatters.ReportFormatter;
import com.haulmont.yarg.formatters.impl.*;
import com.haulmont.yarg.formatters.impl.doc.connector.OfficeIntegrationAPI;
import com.haulmont.yarg.formatters.impl.xls.XlsToPdfConverter;
import com.haulmont.yarg.formatters.impl.xls.XlsToPdfConverterAPI;
import com.haulmont.yarg.structure.BandData;
import com.haulmont.yarg.structure.ReportTemplate;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class DefaultFormatterFactory implements ReportFormatterFactory {
    protected OfficeIntegrationAPI officeIntegration;
    protected XlsToPdfConverterAPI xlsToPdfConverter;
    protected DefaultFormatProvider defaultFormatProvider;

    protected Map<String, FormatterCreator> formattersMap = new HashMap<>();

    public DefaultFormatterFactory() {
        formattersMap.put("xls", new FormatterCreator() {
            @Override
            public ReportFormatter create(BandData rootBand, ReportTemplate reportTemplate, OutputStream outputStream) {
                XLSFormatter xlsFormatter = new XLSFormatter(rootBand, reportTemplate, outputStream);
                xlsFormatter.setXlsToPdfConverter(xlsToPdfConverter);
                xlsFormatter.setDefaultFormatProvider(defaultFormatProvider);
                return xlsFormatter;
            }
        });

        FormatterCreator docCreator = new FormatterCreator() {
            @Override
            public ReportFormatter create(BandData rootBand, ReportTemplate reportTemplate, OutputStream outputStream) {
                if (officeIntegration == null) {
                    throw new UnsupportedFormatException("Could not use doc templates because Open Office connection params not set. Please check, that \"cuba.reporting.openoffice.path\" property is set in properties file.");
                }
                DocFormatter docFormatter = new DocFormatter(rootBand, reportTemplate, outputStream, officeIntegration);
                docFormatter.setDefaultFormatProvider(defaultFormatProvider);
                return docFormatter;
            }
        };
        formattersMap.put("odt", docCreator);
        formattersMap.put("doc", docCreator);
        FormatterCreator ftlCreator = new FormatterCreator() {
            @Override
            public ReportFormatter create(BandData rootBand, ReportTemplate reportTemplate, OutputStream outputStream) {
                HtmlFormatter htmlFormatter = new HtmlFormatter(rootBand, reportTemplate, outputStream);
                htmlFormatter.setDefaultFormatProvider(defaultFormatProvider);
                return htmlFormatter;
            }
        };
        formattersMap.put("ftl", ftlCreator);
        formattersMap.put("html", ftlCreator);
        formattersMap.put("docx", new FormatterCreator() {
            @Override
            public ReportFormatter create(BandData rootBand, ReportTemplate reportTemplate, OutputStream outputStream) {
                DocxFormatter docxFormatter = new DocxFormatter(rootBand, reportTemplate, outputStream);
                docxFormatter.setDefaultFormatProvider(defaultFormatProvider);
                return docxFormatter;
            }
        });
        formattersMap.put("xlsx", new FormatterCreator() {
            @Override
            public ReportFormatter create(BandData rootBand, ReportTemplate reportTemplate, OutputStream outputStream) {
                XlsxFormatter xlsxFormatter = new XlsxFormatter(rootBand, reportTemplate, outputStream);
                xlsxFormatter.setDefaultFormatProvider(defaultFormatProvider);
                return xlsxFormatter;
            }
        });
    }

    public void setOfficeIntegration(OfficeIntegrationAPI officeIntegrationAPI) {
        this.officeIntegration = officeIntegrationAPI;
        this.xlsToPdfConverter = new XlsToPdfConverter(officeIntegrationAPI);
    }

    public void setDefaultFormatProvider(DefaultFormatProvider defaultFormatProvider) {
        this.defaultFormatProvider = defaultFormatProvider;
    }

    public ReportFormatter createFormatter(FormatterFactoryInput factoryInput) {
        String templateExtension = factoryInput.templateExtension;
        BandData rootBand = factoryInput.rootBand;
        ReportTemplate reportTemplate = factoryInput.reportTemplate;
        OutputStream outputStream = factoryInput.outputStream;

        FormatterCreator formatterCreator = formattersMap.get(templateExtension);
        if (formatterCreator == null) {
            throw new UnsupportedFormatException(String.format("Unsupported template extension [%s]", templateExtension));
        }

        return formatterCreator.create(rootBand, reportTemplate, outputStream);
    }

    protected static interface FormatterCreator {
        ReportFormatter create(BandData rootBand, ReportTemplate reportTemplate, OutputStream outputStream);
    }
}
