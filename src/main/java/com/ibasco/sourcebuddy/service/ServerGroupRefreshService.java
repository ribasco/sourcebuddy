package com.ibasco.sourcebuddy.service;

import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import org.springframework.stereotype.Service;

@Service
public class ServerGroupRefreshService extends ScheduledService<Void> {
    @Override
    protected Task<Void> createTask() {
        return null;
    }
}
