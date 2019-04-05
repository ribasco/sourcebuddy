package com.ibasco.sourcebuddy.util;

import javafx.application.Preloader;

public class InfoNotification implements Preloader.PreloaderNotification {

    private String message;

    public InfoNotification(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
