package com.ibasco.sourcebuddy.enums;

public enum ServerStatus {
    NEW(-1, "New"),
    ACTIVE(0, "Active"),
    TIMED_OUT(1, "Timed Out"),
    ERRORED(2, "Error"),
    INACTIVE(3, "Inactive"),
    PURGED(4, "Purged");

    private int status;

    private String description;

    ServerStatus(int status, String description) {
        this.status = status;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public int getStatus() {
        return status;
    }
}