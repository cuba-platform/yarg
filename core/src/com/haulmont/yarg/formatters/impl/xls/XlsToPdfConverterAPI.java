/**
 *
 * @author degtyarjov
 * @version $Id$
 */
package com.haulmont.yarg.formatters.impl.xls;

import java.io.OutputStream;

public interface XlsToPdfConverterAPI {
    void convertXlsToPdf(byte[] documentBytes, OutputStream outputStream);
}
