/**
 *
 * @author degtyarjov
 * @version $Id$
 */
package com.haulmont.yarg.structure;

import com.haulmont.yarg.formatters.CustomReport;

import java.io.InputStream;

/**
 * This interface describes report template document.
  */
public interface ReportTemplate {
    public static final String DEFAULT_TEMPLATE_CODE = "DEFAULT";

    String getCode();

    String getDocumentName();

    String getDocumentPath();

    /**
     * @return stream containing resulting document
     */
    InputStream getDocumentContent();

    /**
     * @return output type of for this template
     */
    ReportOutputType getOutputType();

    /**
     * @return name pattern for generating document. Example: ${Band1.FILE_NAME} or myDocument.doc
     */
    String getOutputNamePattern();

    /**
     * @return if report is defined by custom class.
     * In this case band data will be passed in com.haulmont.yarg.structure.ReportTemplate#getCustomReport() object and it will generate binary.
     */
    boolean isCustom();

    /**
     * @return implementation of custom report logic.
     */
    CustomReport getCustomReport();
}
