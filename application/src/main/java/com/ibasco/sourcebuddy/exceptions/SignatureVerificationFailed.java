package com.ibasco.sourcebuddy.exceptions;

import java.util.concurrent.CompletionException;

public class SignatureVerificationFailed extends CompletionException {

    public SignatureVerificationFailed() {
    }

    public SignatureVerificationFailed(String message) {
        super(message);
    }

    public SignatureVerificationFailed(String message, Throwable cause) {
        super(message, cause);
    }

    public SignatureVerificationFailed(Throwable cause) {
        super(cause);
    }
}
