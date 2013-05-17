/**
 *
 * @author degtyarjov
 * @version $Id$
 */
package com.haulmont.newreport.loaders;

public interface ParametersConverter {
    <T> T convert(Object input);
}
