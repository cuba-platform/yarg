/*
 * Copyright (c) 2011 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Artamonov Yuryi
 * Created: 29.03.11 17:47
 *
 * $Id: UnsupportedFormatException.java 6311 2011-10-20 12:06:16Z artamonov $
 */
package com.haulmont.yarg.exception;

public class UnsupportedFormatException extends ReportingException {
    public UnsupportedFormatException() {
    }

    public UnsupportedFormatException(String message) {
        super(message);
    }

    public UnsupportedFormatException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnsupportedFormatException(Throwable cause) {
        super(cause);
    }
}
