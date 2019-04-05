package com.ibasco.sourcebuddy.exceptions;

public class NoMappedControllerException extends ViewLoadException {

    public NoMappedControllerException() {
    }

    public NoMappedControllerException(String message) {
        super(message);
    }

    public NoMappedControllerException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoMappedControllerException(Throwable cause) {
        super(cause);
    }

    public NoMappedControllerException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
