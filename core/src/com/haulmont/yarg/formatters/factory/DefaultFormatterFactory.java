/**
 *
 * @author degtyarjov
 * @version $Id$
 */
package com.haulmont.yarg.formatters.factory;

import com.haulmont.yarg.exception.UnsupportedFormatException;
import com.haulmont.yarg.formatters.*;
import com.haulmont.yarg.formatters.impl.DocFormatter;
import com.haulmont.yarg.formatters.impl.DocxFormatter;
import com.haulmont.yarg.formatters.impl.HtmlFormatter;
import com.haulmont.yarg.formatters.impl.XLSFormatter;
import com.haulmont.yarg.formatters.impl.doc.connector.OfficeIntegrationAPI;
import com.haulmont.yarg.formatters.impl.xls.XlsToPdfConverter;
import com.haulmont.yarg.formatters.impl.xls.XlsToPdfConverterAPI;
import com.haulmont.yarg.structure.impl.BandData;
import com.haulmont.yarg.structure.ReportTemplate;

import java.io.OutputStream;

public class DefaultFormatterFactory implements ReportFormatterFactory {
    protected OfficeIntegrationAPI officeIntegration;
    protected XlsToPdfConverterAPI xlsToPdfConverter;

    public DefaultFormatterFactory() {
    }

    public void setOfficeIntegration(OfficeIntegrationAPI officeIntegrationAPI) {
        this.officeIntegration = officeIntegrationAPI;
        this.xlsToPdfConverter = new XlsToPdfConverter(officeIntegrationAPI);
    }

    public ReportFormatter createFormatter(FormatterFactoryInput factoryInput) {
        String templateExtension = factoryInput.templateExtension;
        BandData rootBand = factoryInput.rootBand;
        ReportTemplate reportTemplate = factoryInput.reportTemplate;
        OutputStream outputStream = factoryInput.outputStream;

        if ("xls".equalsIgnoreCase(templateExtension)) {
            XLSFormatter xlsFormatter = new XLSFormatter(rootBand, reportTemplate, outputStream);
            xlsFormatter.setXlsToPdfConverter(xlsToPdfConverter);
            return xlsFormatter;
        } else if ("doc".equalsIgnoreCase(templateExtension) || "odt".equalsIgnoreCase(templateExtension)) {
            if (officeIntegration == null) {
                throw new UnsupportedFormatException("Could not use doc templates because Open Office connection params not set. Please check, that \"cuba.reporting.openoffice.path\" property is set in properties file.");
            }
            return new DocFormatter(rootBand, reportTemplate, outputStream, officeIntegration);
        } else if ("docx".equalsIgnoreCase(templateExtension)) {
            return new DocxFormatter(rootBand, reportTemplate, outputStream);
        } else if ("ftl".equalsIgnoreCase(templateExtension)) {
            return new HtmlFormatter(rootBand, reportTemplate, outputStream);
        } else if ("html".equalsIgnoreCase(templateExtension)) {
            return new HtmlFormatter(rootBand, reportTemplate, outputStream);
        }

        throw new UnsupportedFormatException(String.format("Unsupported template extension [%s]", templateExtension));
    }
}
