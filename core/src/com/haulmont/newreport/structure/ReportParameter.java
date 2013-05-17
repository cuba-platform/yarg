/**
 *
 * @author degtyarjov
 * @version $Id$
 */
package com.haulmont.newreport.structure;

public interface ReportParameter {
    String getName();

    String getAlias();

    Boolean getRequired();

    Class getType();
}
