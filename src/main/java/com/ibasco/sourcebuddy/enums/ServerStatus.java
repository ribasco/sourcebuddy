package com.ibasco.sourcebuddy.enums;

public enum ServerStatus {
    NEW(-1),
    ACTIVE(0),
    TIMED_OUT(1),
    ERRORED(2),
    INACTIVE(3),
    PURGED(4);

    private int status;

    ServerStatus(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}