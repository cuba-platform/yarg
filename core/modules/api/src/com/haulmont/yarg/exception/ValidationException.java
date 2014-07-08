/**
 *
 * @author degtyarjov
 * @version $Id$
 */
package com.haulmont.yarg.exception;


/**
 * Thrown when data loader met invalid input parameters
 */
public class ValidationException extends ReportingException {
    public ValidationException() {
    }

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ValidationException(Throwable cause) {
        super(cause);
    }
}
