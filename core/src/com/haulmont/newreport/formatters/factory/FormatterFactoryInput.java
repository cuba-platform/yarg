/**
 *
 * @author degtyarjov
 * @version $Id: FormatterFactoryInput.java 11666 2013-05-16 17:33:03Z degtyarjov $
 */
package com.haulmont.newreport.formatters.factory;

import com.haulmont.newreport.structure.impl.Band;
import com.haulmont.newreport.structure.ReportTemplate;

import java.io.OutputStream;

public class FormatterFactoryInput {

    protected String templateExtension;
    protected Band rootBand;
    protected ReportTemplate reportTemplate;
    protected OutputStream outputStream;

    public FormatterFactoryInput(String templateExtension, Band rootBand, ReportTemplate reportTemplate, OutputStream outputStream) {
        if (templateExtension == null) {
            throw new NullPointerException("templateExtension can not be null");
        }

        if (rootBand == null) {
            throw new NullPointerException("rootBand can not be null");
        }

        this.templateExtension = templateExtension;
        this.rootBand = rootBand;
        this.reportTemplate = reportTemplate;
        this.outputStream = outputStream;
    }
}
