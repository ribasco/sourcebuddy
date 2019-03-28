package com.ibasco.sourcebuddy.components;

import com.ibasco.sourcebuddy.constants.Qualifiers;
import com.ibasco.sourcebuddy.tasks.BaseTask;
import com.ibasco.sourcebuddy.util.SpringUtil;
import javafx.beans.property.Property;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
public class TaskManager implements Closeable {

    private static final Logger log = LoggerFactory.getLogger(TaskManager.class);

    private ObservableList<Task<?>> taskList = FXCollections.observableArrayList();

    private ThreadPoolExecutor executorService;

    @Autowired
    public TaskManager(@Qualifier(Qualifiers.TASK_EXECUTOR_SERVICE) ExecutorService executorService) {
        this.executorService = (ThreadPoolExecutor) executorService;
    }

    public void executeTask(Task<?> task) {
        if (task == null)
            throw new IllegalArgumentException("Task cannot be null");
        if (taskList.contains(task))
            throw new IllegalStateException("Task is already on the list: " + task);
        attachTaskMonitor(task);
        executorService.execute(task);
        taskList.add(task);
        log.debug("executeTask() :: Submitted task '{}' on pool (Active: {}, Max Pool Size: {})", task.getClass().getSimpleName(), executorService.getActiveCount(), executorService.getMaximumPoolSize());
    }

    public <T extends Task<?>> void executeTask(Class<T> taskClass, Object... args) {
        if (taskClass == null)
            throw new IllegalArgumentException("Task class cannot be null");
        T task = SpringUtil.getBean(taskClass, args);
        if (task == null)
            throw new IllegalStateException("Could not locate task: " + taskClass.getSimpleName());
        executeTask(task);
    }

    private void attachTaskMonitor(Task<?> task) {
        task.stateProperty().addListener(this::taskListener);
    }

    private void detachTaskMonitor(Task<?> task) {
        task.stateProperty().removeListener(this::taskListener);
    }

    public List<Task> getRunningTasks() {
        return taskList.stream().filter(p -> p.getState().equals(Worker.State.RUNNING)).collect(Collectors.toList());
    }

    public long getRunningTasksCount() {
        return taskList.stream().filter(p -> p.getState().equals(Worker.State.RUNNING)).count();
    }

    private void taskListener(ObservableValue<? extends Worker.State> stateProperty, Worker.State oldState, Worker.State newState) {

        @SuppressWarnings("unchecked")
        Property<Worker.State> property = (Property<Worker.State>) stateProperty;
        Task<?> task = (Task<?>) property.getBean();

        String taskName = task.getClass().getSimpleName();

        switch (newState) {
            case READY:
                //log.debug("TASK_READY :: {}", taskName);
                break;
            case SCHEDULED:
                //log.debug("TASK_SCHEDULED :: {}", taskName);
                break;
            case RUNNING:
                //log.debug("TASK_RUNNING :: {}", taskName);
                break;
            case CANCELLED:
                //log.debug("TASK_CANCELLED :: {}", taskName);
                detachTaskMonitor(task);
                taskList.remove(task);
                break;
            case FAILED:
                //log.debug("TASK_FAILED :: {}", taskName);
                Throwable err = task.getException();
                if (err != null) {
                    log.error("Exception occured during task run", err);
                }
                detachTaskMonitor(task);
                taskList.remove(task);
                break;
            case SUCCEEDED:
                //log.debug("TASK_SUCCESS :: {}", taskName);
                detachTaskMonitor(task);
                taskList.remove(task);
                break;
            default:
                break;
        }

        if ((task instanceof BaseTask) && task.isDone()) {
            BaseTask baseTask = (BaseTask) task;
            if (baseTask.getDuration() != null) {
                log.debug("TASK_DURATION :: {} = {} seconds", taskName, baseTask.getDuration().toSeconds());
            }
        }
    }

    public ObservableList<Task<?>> getTaskList() {
        return taskList;
    }

    @Override
    public void close() throws IOException {
        try {
            this.executorService.shutdownNow();
            this.executorService.awaitTermination(3, TimeUnit.SECONDS);
        } catch (InterruptedException ignored) {

        }
    }
}
