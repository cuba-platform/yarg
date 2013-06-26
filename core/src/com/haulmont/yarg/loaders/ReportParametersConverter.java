/**
 *
 * @author degtyarjov
 * @version $Id$
 */
package com.haulmont.yarg.loaders;

public interface ReportParametersConverter {
    <T> T convert(Object input);
}
