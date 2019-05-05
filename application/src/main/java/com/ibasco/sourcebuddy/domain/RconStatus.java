package com.ibasco.sourcebuddy.domain;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class RconStatus {

    private BooleanProperty authenticated = new SimpleBooleanProperty();

    private StringProperty reason = new SimpleStringProperty();

    public RconStatus(Boolean authenticated, String reason) {
        setAuthenticated(authenticated);
        setReason(reason);
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
}
