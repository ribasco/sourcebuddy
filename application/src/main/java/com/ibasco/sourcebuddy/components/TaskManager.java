package com.ibasco.sourcebuddy.components;

import com.ibasco.sourcebuddy.constants.Qualifiers;
import com.ibasco.sourcebuddy.tasks.BaseTask;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
public class TaskManager implements Closeable {

    private static final Logger log = LoggerFactory.getLogger(TaskManager.class);

    private ObservableList<Task<?>> taskList = FXCollections.observableArrayList();

    private Map<Task<?>, CompletableFuture<Void>> taskMap = new HashMap<>();

    private ThreadPoolExecutor executorService;

    private SpringHelper springHelper;

    @Autowired
    public TaskManager(@Qualifier(Qualifiers.TASK_EXECUTOR_SERVICE) ExecutorService executorService) {
        this.executorService = (ThreadPoolExecutor) executorService;
    }

    public <T extends Task<?>> CompletableFuture<Void> runTask(Class<T> taskClass, Object... args) {
        if (taskClass == null)
            throw new IllegalArgumentException("Task class cannot be null");
        T task = springHelper.getBean(taskClass, args);
        if (task == null)
            throw new IllegalStateException("Could not locate task: " + taskClass.getSimpleName());
        CompletableFuture<Void> cf = new CompletableFuture<>();
        taskMap.put(task, cf);
        attachTaskMonitor(task);
        executorService.execute(task);
        log.debug("runTask() :: Submitted task '{}' on pool (Active: {}, Max Pool Size: {})", task.getClass().getSimpleName(), executorService.getActiveCount(), executorService.getMaximumPoolSize());
        return cf;
    }

    @Deprecated
    public <T extends Task<?>> T executeTask(Class<T> taskClass, Object... args) {
        if (taskClass == null)
            throw new IllegalArgumentException("Task class cannot be null");
        T task = springHelper.getBean(taskClass, args);
        if (task == null)
            throw new IllegalStateException("Could not locate task: " + taskClass.getSimpleName());
        executeTask(task);
        return task;
    }

    @Deprecated
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
                if (!taskList.contains(task))
                    taskList.add(task);
                break;
            case RUNNING:
                //log.debug("TASK_RUNNING :: {}", taskName);
                break;
            case CANCELLED:
                //log.debug("TASK_CANCELLED :: {}", taskName);
                if (taskMap.containsKey(task)) {
                    CompletableFuture<Void> cf = taskMap.get(task);
                    cf.completeExceptionally(new InterruptedException("Task cancelled"));
                    taskMap.remove(task);
                }
                detachTaskMonitor(task);
                taskList.remove(task);
                break;
            case FAILED:
                //log.debug("TASK_FAILED :: {}", taskName);
                Throwable err = task.getException();
                log.error("Exception occured during task run", err);
                if (taskMap.containsKey(task)) {
                    CompletableFuture<Void> cf = taskMap.get(task);
                    cf.completeExceptionally(err);
                    taskMap.remove(task);
                }
                detachTaskMonitor(task);
                taskList.remove(task);
                break;
            case SUCCEEDED:
                //log.debug("TASK_SUCCESS :: {}", taskName);
                if (taskMap.containsKey(task)) {
                    CompletableFuture<Void> cf = taskMap.get(task);
                    cf.complete(null);
                    taskMap.remove(task);
                }
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

    @Autowired
    public void setSpringHelper(SpringHelper springHelper) {
        this.springHelper = springHelper;
    }
}
