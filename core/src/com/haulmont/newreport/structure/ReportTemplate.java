/**
 *
 * @author degtyarjov
 * @version $Id$
 */
package com.haulmont.newreport.structure;

import com.haulmont.newreport.structure.ReportOutputType;

import java.io.InputStream;

public interface ReportTemplate {
    public static final String DEFAULT_TEMPLATE_CODE = "DEFAULT";

    String getCode();

    String getDocumentName();

    String getDocumentPath();

    InputStream getDocumentContent();

    ReportOutputType getOutputType();
}
