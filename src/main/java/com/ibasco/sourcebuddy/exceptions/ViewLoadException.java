package com.ibasco.sourcebuddy.exceptions;

public class ViewLoadException extends RuntimeException {

    public ViewLoadException() {
    }

    public ViewLoadException(String message) {
        super(message);
    }

    public ViewLoadException(String message, Throwable cause) {
        super(message, cause);
    }

    public ViewLoadException(Throwable cause) {
        super(cause);
    }

    public ViewLoadException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
