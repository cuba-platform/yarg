/*
 * Copyright (c) 2008 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Eugeniy Degtyarjov
 * Created: 13.05.2010 9:59:29
 *
 * $Id: Formatter.java 6657 2011-12-05 08:56:42Z degtyarjov $
 */
package com.haulmont.yarg.formatters;

/**
 * This interface describes a logic which construct resulting document
 */
public interface ReportFormatter {

    /**
     * Creates document and put it into byte array
     */
    byte[] createDocument();

    /**
     * Creates document and serialize it to predefined stream or smth like this
     */
    void renderDocument();
}
