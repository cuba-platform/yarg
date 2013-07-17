/**
 *
 * @author degtyarjov
 * @version $Id$
 */
package com.haulmont.yarg.loaders;

/**
 * This interface describes logic which transform input parameters. Example: convert Entity object to entity id for sql data loader.
 */
public interface ReportParametersConverter {
    <T> T convert(Object input);
}
