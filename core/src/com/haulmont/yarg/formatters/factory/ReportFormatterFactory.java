/**
 *
 * @author degtyarjov
 * @version $Id$
 */
package com.haulmont.yarg.formatters.factory;

import com.haulmont.yarg.formatters.ReportFormatter;

public interface ReportFormatterFactory {
    ReportFormatter createFormatter(FormatterFactoryInput factoryInput);
}
