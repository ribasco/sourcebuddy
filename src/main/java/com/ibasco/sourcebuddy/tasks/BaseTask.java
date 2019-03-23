package com.ibasco.sourcebuddy.tasks;

import javafx.concurrent.Task;

abstract class BaseTask<T> extends Task<T> {

    void updateMessage(String message, Object... args) {
        String formattedMsg = String.format(message, args);
        super.updateMessage(formattedMsg);
    }
}
