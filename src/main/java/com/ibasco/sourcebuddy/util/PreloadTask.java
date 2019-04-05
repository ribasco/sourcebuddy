package com.ibasco.sourcebuddy.util;

import com.ibasco.sourcebuddy.model.PreloadModel;
import javafx.application.Platform;
import javafx.concurrent.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
abstract public class PreloadTask extends Task<Void> {

    private static final Logger log = LoggerFactory.getLogger(PreloadTask.class);

    private PreloadModel preloadModel;

    @Override
    protected final Void call() throws Exception {
        preload();
        return null;
    }

    abstract public void preload() throws Exception;

    protected void indeterminateProgress() {
        updateProgress(-1);
    }

    protected void updateProgress(double progress) {
        updateProgress(progress, -1);
    }

    @Override
    protected void updateProgress(long workDone, long max) {
        updateProgress((double) workDone, (double) max);
    }

    @Override
    protected void updateProgress(double workDone, double max) {
        Platform.runLater(() -> {
            super.updateProgress(workDone, max);
            if (workDone < -1 || max < 0) {
                preloadModel.setProgress(-1);
            } else {
                preloadModel.setProgress(workDone / max);
            }
        });
    }

    @Override
    protected void updateMessage(String message) {
        log.debug("updateMessage() :: {}", message);
        Platform.runLater(() -> {
            super.updateMessage(message);
            preloadModel.setMessage(message);
        });
    }

    protected void updateMessage(String message, Object... args) {
        updateMessage(String.format(message, args));
    }

    @Autowired
    protected void setPreloadModel(PreloadModel preloadModel) {
        this.preloadModel = preloadModel;
    }
}
