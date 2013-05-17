/*
 * Copyright (c) 2008 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Eugeniy Degtyarjov
 * Created: 13.05.2010 9:59:29
 *
 * $Id: Formatter.java 6657 2011-12-05 08:56:42Z degtyarjov $
 */
package com.haulmont.newreport.formatters;

/**
 * Stateful classes, converting report definition, loaded data and parameters into resulting document
 */
public interface Formatter {
    byte[] createDocument();

    void renderDocument();
}
