/*
 * Copyright (c) 2010 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Vasiliy Fontanenko
 * Created: 22.06.2010 13:57:49
 *
 * $Id$
 */
package com.haulmont.newreport.exception;

public class FailedToConnectToOpenOfficeException extends ReportingException {
    public FailedToConnectToOpenOfficeException() {
    }

    public FailedToConnectToOpenOfficeException(String message) {
        super(message);
    }

    public FailedToConnectToOpenOfficeException(String message, Throwable cause) {
        super(message, cause);
    }

    public FailedToConnectToOpenOfficeException(Throwable cause) {
        super(cause);
    }
}
