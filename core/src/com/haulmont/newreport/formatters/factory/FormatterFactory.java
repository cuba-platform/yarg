/**
 *
 * @author degtyarjov
 * @version $Id$
 */
package com.haulmont.newreport.formatters.factory;

import com.haulmont.newreport.formatters.Formatter;

public interface FormatterFactory {
    Formatter createFormatter(FormatterFactoryInput factoryInput);
}
