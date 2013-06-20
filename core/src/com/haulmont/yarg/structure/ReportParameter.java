/**
 *
 * @author degtyarjov
 * @version $Id$
 */
package com.haulmont.yarg.structure;

public interface ReportParameter {
    String getName();

    String getAlias();

    Boolean getRequired();

    Class getParameterClass();
}
