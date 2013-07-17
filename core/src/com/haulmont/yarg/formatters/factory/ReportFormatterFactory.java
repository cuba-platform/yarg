/**
 *
 * @author degtyarjov
 * @version $Id$
 */
package com.haulmont.yarg.formatters.factory;

import com.haulmont.yarg.formatters.ReportFormatter;

/**
 * This interface describes a factory which spawns formatters. The default implementation is com.haulmont.yarg.formatters.factory.DefaultFormatterFactory
 */
public interface ReportFormatterFactory {
    ReportFormatter createFormatter(FormatterFactoryInput factoryInput);
}
