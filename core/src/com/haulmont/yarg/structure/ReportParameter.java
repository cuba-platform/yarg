/**
 *
 * @author degtyarjov
 * @version $Id$
 */
package com.haulmont.yarg.structure;

/**
 * This interface describes report input parameter
 */
public interface ReportParameter {
    /**
     * @return user friendly name
     */
    String getName();

    /**
     * @return alias which is used in formatters and data loaders
     */
    String getAlias();

    Boolean getRequired();

    /**
     * @return parameter class eg Date or String, etc
     */
    Class getParameterClass();
}
