/**
 *
 * @author degtyarjov
 * @version $Id$
 */
package com.haulmont.yarg.structure;

/**
 * This interface describes a format for certain result field.
 */
public interface ReportFieldFormat {
    /**
     * @return formatted field name. Should also contain all parent band names.
     * Example: Band1.Band2.field1
     */
    String getName();

    /**
     * @return format string
     * Example: ##,# for decimals, dd-MM-yyyy for dates, etc.
     */
    String getFormat();
}
