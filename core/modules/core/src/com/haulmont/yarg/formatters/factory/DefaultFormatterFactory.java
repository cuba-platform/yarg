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
package com.haulmont.yarg.formatters.factory;

import com.haulmont.yarg.exception.UnsupportedFormatException;
import com.haulmont.yarg.formatters.ReportFormatter;
import com.haulmont.yarg.formatters.impl.*;
import com.haulmont.yarg.formatters.impl.doc.connector.OfficeIntegrationAPI;
import com.haulmont.yarg.formatters.impl.xls.DocumentConverter;
import com.haulmont.yarg.formatters.impl.xls.DocumentConverterImpl;
import com.haulmont.yarg.structure.BandData;
import com.haulmont.yarg.structure.ReportTemplate;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class DefaultFormatterFactory implements ReportFormatterFactory {
    protected OfficeIntegrationAPI officeIntegration;
    protected DocumentConverter documentConverter;
    protected DefaultFormatProvider defaultFormatProvider;

    protected Map<String, FormatterCreator> formattersMap = new HashMap<String, FormatterCreator>();

    public DefaultFormatterFactory() {
        formattersMap.put("xls", factoryInput -> {
            XLSFormatter xlsFormatter = new XLSFormatter(factoryInput);
            xlsFormatter.setDocumentConverter(documentConverter);
            xlsFormatter.setDefaultFormatProvider(defaultFormatProvider);
            return xlsFormatter;
        });

        FormatterCreator docCreator = factoryInput -> {
            if (officeIntegration == null) {
                throw new UnsupportedFormatException("Could not use doc templates because Open Office connection params not set. Please check, that \"cuba.reporting.openoffice.path\" property is set in properties file.");
            }
            DocFormatter docFormatter = new DocFormatter(factoryInput, officeIntegration);
            docFormatter.setDefaultFormatProvider(defaultFormatProvider);
            return docFormatter;
        };
        formattersMap.put("odt", docCreator);
        formattersMap.put("doc", docCreator);
        FormatterCreator ftlCreator = factoryInput -> {
            HtmlFormatter htmlFormatter = new HtmlFormatter(factoryInput);
            htmlFormatter.setDefaultFormatProvider(defaultFormatProvider);
            return htmlFormatter;
        };
        formattersMap.put("ftl", ftlCreator);
        formattersMap.put("html", ftlCreator);
        formattersMap.put("docx", factoryInput -> {
            DocxFormatter docxFormatter = new DocxFormatter(factoryInput);
            docxFormatter.setDefaultFormatProvider(defaultFormatProvider);
            docxFormatter.setDocumentConverter(documentConverter);
            return docxFormatter;
        });
        FormatterCreator xlsxCreator = factoryInput -> {
            XlsxFormatter xlsxFormatter = new XlsxFormatter(factoryInput);
            xlsxFormatter.setDefaultFormatProvider(defaultFormatProvider);
            xlsxFormatter.setDocumentConverter(documentConverter);
            return xlsxFormatter;
        };
        formattersMap.put("xlsx", xlsxCreator);
        formattersMap.put("xlsm", xlsxCreator);
        formattersMap.put("csv", CsvFormatter::new);
    }

    public void setOfficeIntegration(OfficeIntegrationAPI officeIntegrationAPI) {
        this.officeIntegration = officeIntegrationAPI;
        this.documentConverter = new DocumentConverterImpl(officeIntegrationAPI);
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

        return formatterCreator.create(factoryInput);
    }

    protected static interface FormatterCreator {
        ReportFormatter create(FormatterFactoryInput formatterFactoryInput);
    }
}
