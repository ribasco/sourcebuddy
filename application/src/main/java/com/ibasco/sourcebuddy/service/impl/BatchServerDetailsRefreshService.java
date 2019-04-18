package com.ibasco.sourcebuddy.service.impl;

import com.ibasco.sourcebuddy.service.ListenableTaskService;
import javafx.concurrent.Task;

public class BatchServerDetailsRefreshService extends ListenableTaskService<Void> {

    @Override
    protected void initialize() {

    }

    @Override
    protected Task<Void> createNewTask() {
        return null;
    }
}
