package com.ibasco.sourcebuddy.domain;

import javafx.beans.property.*;

public class RconAuthStatus {

    private BooleanProperty authenticated = new SimpleBooleanProperty();

    private StringProperty reason = new SimpleStringProperty();

    private ObjectProperty<Throwable> exception = new SimpleObjectProperty<>();

    public RconAuthStatus(Boolean authenticated, String reason) {
        this(authenticated, reason, null);
    }

    public RconAuthStatus(Boolean authenticated, String reason, Throwable exception) {
        setAuthenticated(authenticated);
        setReason(reason);
        setException(exception);
    }

    public boolean isAuthenticated() {
        return authenticated.get();
    }

    public BooleanProperty authenticatedProperty() {
        return authenticated;
    }

    public void setAuthenticated(boolean authenticated) {
        this.authenticated.set(authenticated);
    }

    public String getReason() {
        return reason.get();
    }

    public StringProperty reasonProperty() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason.set(reason);
    }

    public Throwable getException() {
        return exception.get();
    }

    public ObjectProperty<Throwable> exceptionProperty() {
        return exception;
    }

    public void setException(Throwable exception) {
        this.exception.set(exception);
    }
}
