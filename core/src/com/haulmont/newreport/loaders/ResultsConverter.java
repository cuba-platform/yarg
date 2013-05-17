/**
 *
 * @author degtyarjov
 * @version $Id$
 */
package com.haulmont.newreport.loaders;

public interface ResultsConverter {
    <T> T convert(Object input);
}
