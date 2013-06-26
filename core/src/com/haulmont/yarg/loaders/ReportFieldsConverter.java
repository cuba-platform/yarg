/**
 *
 * @author degtyarjov
 * @version $Id$
 */
package com.haulmont.yarg.loaders;

public interface ReportFieldsConverter {
    <T> T convert(Object input);
}
