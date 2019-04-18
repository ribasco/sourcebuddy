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

    private Throwable exception;

    ServerStatus(int status, String description) {
        this(status, description, null);
    }

    ServerStatus(int status, String description, Throwable exception) {
        this.status = status;
        this.description = description;
        this.exception = exception;
    }

    public String getDescription() {
        return description;
    }

    public Throwable getException() {
        return exception;
    }

    public int getStatus() {
        return status;
    }

    public static ServerStatus onError(Throwable ex) {
        ServerStatus status = ServerStatus.ERRORED;
        status.exception = ex;
        return status;
    }
}