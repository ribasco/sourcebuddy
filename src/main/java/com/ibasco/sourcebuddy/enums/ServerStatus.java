package com.ibasco.sourcebuddy.enums;

public enum ServerStatus {
    ACTIVE(0),
    INACTIVE(1),
    PURGED(2);

    private int status;

    ServerStatus(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}