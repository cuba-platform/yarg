/**
 *
 * @author degtyarjov
 * @version $Id$
 */
package com.haulmont.yarg.exception;

public class ReportFormattingException extends ReportingException {
    public ReportFormattingException() {
    }

    public ReportFormattingException(String message) {
        super(message);
    }

    public ReportFormattingException(String message, Throwable cause) {
        super(message, cause);
    }

    public ReportFormattingException(Throwable cause) {
        super(cause);
    }
}
