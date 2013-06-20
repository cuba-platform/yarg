/*
 * Copyright (c) 2012 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: degtyarjov
 * Created: 24.01.13 12:20
 *
 * $Id$
 */
package com.haulmont.yarg.formatters.impl.doc.connector;

public class NoFreePortsException extends Exception {
    private static final long serialVersionUID = 727618681331262033L;

    public NoFreePortsException() {
    }

    public NoFreePortsException(String message) {
        super(message);
    }

    public NoFreePortsException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoFreePortsException(Throwable cause) {
        super(cause);
    }
}
