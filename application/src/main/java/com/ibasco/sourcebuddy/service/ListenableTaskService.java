package com.ibasco.sourcebuddy.service;

import com.ibasco.sourcebuddy.annotations.BaseComponent;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;

import javax.annotation.PostConstruct;
import java.util.concurrent.ExecutorService;

@BaseComponent
abstract public class ListenableTaskService<T> extends ScheduledService<T> {

    private static final Logger log = LoggerFactory.getLogger(ListenableTaskService.class);

    private ReadOnlyObjectWrapper<Task<T>> task = new ReadOnlyObjectWrapper<>();

    private ExecutorService scheduledTaskService;

    private ConfigurableApplicationContext applicationContext;

    @PostConstruct
    private void initializeBase() {
        log.debug("Initializing default properties for service: {}", this.getClass().getSimpleName());
        setExecutor(scheduledTaskService);
        this.initialize();
    }

    abstract protected void initialize();

    @Override
    protected final Task<T> createTask() {
        Task<T> task = createNewTask();
        this.task.set(task);
        return task;
    }

    abstract protected Task<T> createNewTask();

    public final Task getTask() {
        return task.get();
    }

    public final ReadOnlyObjectProperty<Task<T>> taskProperty() {
        return task;
    }

    protected ExecutorService getScheduledTaskService() {
        return scheduledTaskService;
    }

    protected final ConfigurableApplicationContext getApplicationContext() {
        return applicationContext;
    }

    @Autowired
    protected void setApplicationContext(ConfigurableApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Autowired
    protected void setScheduledTaskService(ExecutorService scheduledTaskService) {
        this.scheduledTaskService = scheduledTaskService;
    }
}
