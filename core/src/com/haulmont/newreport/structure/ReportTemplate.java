/**
 *
 * @author degtyarjov
 * @version $Id$
 */
package com.haulmont.newreport.structure;

import com.haulmont.newreport.formatters.CustomReport;

import java.io.InputStream;

public interface ReportTemplate {
    public static final String DEFAULT_TEMPLATE_CODE = "DEFAULT";

    String getCode();

    String getDocumentName();

    String getDocumentPath();

    InputStream getDocumentContent();

    ReportOutputType getOutputType();

    String getOutputNamePattern();

    boolean isCustom();

    CustomReport getCustomReport();
}
