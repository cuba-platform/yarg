/**
 *
 * @author degtyarjov
 * @version $Id$
 */
package com.haulmont.yarg.loaders;

public interface ParametersConverter {
    <T> T convert(Object input);
}
