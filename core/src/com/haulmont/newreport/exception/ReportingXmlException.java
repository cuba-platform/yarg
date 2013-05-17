/*
 * Copyright (c) 2011 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Artamonov Yuryi
 * Created: 29.03.11 17:47
 *
 * $Id: UnsupportedFormatException.java 6311 2011-10-20 12:06:16Z artamonov $
 */
package com.haulmont.newreport.exception;

public class ReportingXmlException extends ReportingException {
    public ReportingXmlException() {
    }

    public ReportingXmlException(String message) {
        super(message);
    }

    public ReportingXmlException(String message, Throwable cause) {
        super(message, cause);
    }

    public ReportingXmlException(Throwable cause) {
        super(cause);
    }
}
