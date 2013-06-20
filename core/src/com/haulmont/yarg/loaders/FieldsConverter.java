/**
 *
 * @author degtyarjov
 * @version $Id$
 */
package com.haulmont.yarg.loaders;

public interface FieldsConverter {
    <T> T convert(Object input);
}
