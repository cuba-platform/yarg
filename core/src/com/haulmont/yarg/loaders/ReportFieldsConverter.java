/**
 *
 * @author degtyarjov
 * @version $Id$
 */
package com.haulmont.yarg.loaders;

/**
 * This interface describes logic which transform resulting values. Example: convert PGObject from postgresql to java.util.UUID
 */
public interface ReportFieldsConverter {
    <T> T convert(Object input);
}
