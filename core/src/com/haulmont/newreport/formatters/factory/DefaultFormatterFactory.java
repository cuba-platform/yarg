/**
 *
 * @author degtyarjov
 * @version $Id$
 */
package com.haulmont.newreport.formatters.factory;

import com.haulmont.newreport.exception.UnsupportedFormatException;
import com.haulmont.newreport.formatters.*;
import com.haulmont.newreport.formatters.impl.DocFormatter;
import com.haulmont.newreport.formatters.impl.DocxFormatter;
import com.haulmont.newreport.formatters.impl.HtmlFormatter;
import com.haulmont.newreport.formatters.impl.XLSFormatter;
import com.haulmont.newreport.formatters.impl.doc.connector.OOTaskRunnerAPI;
import com.haulmont.newreport.formatters.impl.xls.XlsToPdfConverter;
import com.haulmont.newreport.formatters.impl.xls.XlsToPdfConverterAPI;
import com.haulmont.newreport.structure.impl.Band;
import com.haulmont.newreport.structure.ReportTemplate;

import java.io.OutputStream;

public class DefaultFormatterFactory implements FormatterFactory {
    protected OOTaskRunnerAPI ooTaskRunnerAPI;
    protected XlsToPdfConverterAPI xlsToPdfConverter;

    public DefaultFormatterFactory() {
    }

    public void setOOTaskRunner(OOTaskRunnerAPI ooTaskRunnerAPI) {
        this.ooTaskRunnerAPI = ooTaskRunnerAPI;
        this.xlsToPdfConverter = new XlsToPdfConverter(ooTaskRunnerAPI);
    }

    public Formatter createFormatter(FormatterFactoryInput factoryInput) {
        String templateExtension = factoryInput.templateExtension;
        Band rootBand = factoryInput.rootBand;
        ReportTemplate reportTemplate = factoryInput.reportTemplate;
        OutputStream outputStream = factoryInput.outputStream;

        if ("xls".equalsIgnoreCase(templateExtension)) {
            XLSFormatter xlsFormatter = new XLSFormatter(rootBand, reportTemplate, outputStream);
            xlsFormatter.setXlsToPdfConverter(xlsToPdfConverter);
            return xlsFormatter;
        } else if ("doc".equalsIgnoreCase(templateExtension) || "odt".equalsIgnoreCase(templateExtension)) {
            if (ooTaskRunnerAPI == null) {
                throw new UnsupportedFormatException("Could not use doc templates because Open Office connection params not set. Please check, that \"cuba.reporting.openoffice.path\" property is set in properties file.");
            }
            return new DocFormatter(rootBand, reportTemplate, outputStream, ooTaskRunnerAPI);
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
