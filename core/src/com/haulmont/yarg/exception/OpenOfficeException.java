/*
 * Copyright (c) 2010 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Vasiliy Fontanenko
 * Created: 22.06.2010 13:57:49
 *
 * $Id$
 */
package com.haulmont.yarg.exception;

public class OpenOfficeException extends ReportingException {
    public OpenOfficeException() {
    }

    public OpenOfficeException(String message) {
        super(message);
    }

    public OpenOfficeException(String message, Throwable cause) {
        super(message, cause);
    }

    public OpenOfficeException(Throwable cause) {
        super(cause);
    }
}
