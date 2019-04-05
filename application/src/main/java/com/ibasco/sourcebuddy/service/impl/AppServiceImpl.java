package com.ibasco.sourcebuddy.service.impl;

import com.ibasco.sourcebuddy.service.AppService;
import com.ibasco.sourcebuddy.util.Check;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Service
public class AppServiceImpl implements AppService {

    private static final Logger log = LoggerFactory.getLogger(AppServiceImpl.class);

    private ScheduledExecutorService scheduledTaskService;

    private Map<Integer, ScheduledTask> scheduledTasks = new HashMap<>();

    class ScheduledTask {

        ScheduledFuture<?> future;

        Duration duration;

        Runnable action;

        ScheduledTask(ScheduledFuture<?> future, Duration duration, Runnable action) {
            this.future = future;
            this.duration = duration;
            this.action = action;
        }
    }

    @Autowired
    public void setScheduledTaskService(ScheduledExecutorService scheduledTaskService) {
        this.scheduledTaskService = scheduledTaskService;
    }

    @Override
    public int runTaskAfter(Duration delay, Runnable action) {
        if (action == null)
            throw new IllegalArgumentException("runTaskAfter () :: Action cannot be null");
        int id = getTaskId(action);

        if (scheduledTasks.containsKey(id)) {
            log.debug("runTaskAfter() :: Task already scheduled: {}", id);
            return -1;
        }

        ScheduledFuture<?> fut = scheduledTaskService.schedule(() -> {
            Platform.runLater(() -> {
                try {
                    action.run();
                } finally {
                    log.debug("runTaskAfter() :: Completed task (total: {})", scheduledTasks.size());
                    scheduledTasks.remove(action.hashCode());
                }
            });
        }, delay.toMillis(), TimeUnit.MILLISECONDS);

        scheduledTasks.put(id, new ScheduledTask(fut, delay, action));
        return id;
    }

    @Override
    public boolean touchTask(Runnable action) {
        Check.requireNonNull(action, "touchTask() :: Action cannot be null");
        int id = getTaskId(action);
        ScheduledTask task = scheduledTasks.get(id);
        if (task != null) {
            if (cancelTask(action)) {
                runTaskAfter(task.duration, task.action);
                log.debug("touchTask() :: Restarting scheduled task = {}", task.action.hashCode());
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean cancelTask(Runnable action) {
        Check.requireNonNull(action, "cancelTask() :: Action cannot be null");
        int id = getTaskId(action);
        if (!scheduledTasks.containsKey(id))
            return false;
        try {
            ScheduledTask task = scheduledTasks.get(id);
            if (task.future.isCancelled()) {
                log.debug("cancelTask() :: Task already cancelled = {}", id);
                return true;
            }
            return task.future.cancel(true);
        } finally {
            scheduledTasks.remove(id);
        }
    }

    private int getTaskId(Runnable action) {
        if (action != null)
            return action.hashCode();
        throw new IllegalStateException("getTaskId() :: Action cannot be null");
    }
}
