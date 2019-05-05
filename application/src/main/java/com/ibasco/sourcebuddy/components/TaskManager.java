package com.ibasco.sourcebuddy.components;

import com.ibasco.sourcebuddy.constants.Qualifiers;
import com.ibasco.sourcebuddy.tasks.BaseTask;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

@Component
public class TaskManager {

    private static final Logger log = LoggerFactory.getLogger(TaskManager.class);

    private ObservableMap<Task<?>, CompletableFuture<?>> taskMap = FXCollections.observableHashMap();

    private IntegerProperty runningTasks = new SimpleIntegerProperty();

    private ThreadPoolExecutor executorService;

    private SpringHelper springHelper;

    @Autowired
    public TaskManager(@Qualifier(Qualifiers.TASK_EXECUTOR_SERVICE) ExecutorService executorService) {
        this.executorService = (ThreadPoolExecutor) executorService;
    }

    public <T extends BaseTask<U>, U> CompletableFuture<U> run(Class<T> taskClass, Object... args) {
        if (taskClass == null)
            throw new IllegalArgumentException("Task class cannot be null");
        T task = springHelper.getBean(taskClass, args);
        return run(task);
    }

    public <T extends BaseTask<U>, U> CompletableFuture<U> run(T task) {
        if (task == null)
            throw new IllegalArgumentException("Task cannot be null");
        CompletableFuture<U> cf = new CompletableFuture<>();
        try {
            cf.whenComplete((u, ex) -> {
                if (ex instanceof CancellationException) {
                    log.debug("Future task detected cancellation event");
                    if (!task.isCancelled()) {
                        log.debug("Cancelling underlying task");
                        task.cancel(true);
                    }
                }
                taskMap.remove(task);
            });
            taskMap.put(task, cf);
            attachTaskMonitor(task);
            log.debug("run() :: Submitted task '{}' on pool (Active: {}, Max Pool Size: {})", task.getClass().getSimpleName(), executorService.getActiveCount(), executorService.getMaximumPoolSize());
        } finally {
            executorService.execute(task);
        }
        return cf;
    }

    public <T extends Task<?>> boolean isRunning(Task<?> task) {
        return taskMap.entrySet().stream().anyMatch(p -> p.getKey().equals(task) && p.getKey().isRunning());
    }

    public <T extends Task<?>> boolean isRunning(Class<T> taskClass) {
        return taskMap.entrySet().stream().anyMatch(p -> p.getKey().getClass().equals(taskClass) && !p.getKey().isRunning());
    }

    public boolean contains(Class<? extends Task<?>> taskClass) {
        return taskMap.entrySet().stream().anyMatch(p -> p.getKey().getClass().equals(taskClass));
    }

    public boolean contains(Task<?> task) {
        return taskMap.containsKey(task);
    }

    public ObservableMap<Task<?>, CompletableFuture<?>> getTaskMap() {
        return taskMap;
    }

    public Optional<CompletableFuture<?>> getFuture(Class<?> taskClass) {
        return taskMap.entrySet().stream().filter(p -> p.getKey().getClass().equals(taskClass)).findFirst().map(Map.Entry::getValue);
    }

    public Optional<Task<?>> getTask(CompletableFuture<?> future) {
        return taskMap.entrySet().stream().filter(p -> p.getValue().equals(future)).findFirst().map(Map.Entry::getKey);
    }

    public Optional<Task<?>> getTask(Class<? extends Task<?>> taskClass) {
        return taskMap.keySet().stream().filter(p -> p.getClass().equals(taskClass)).findFirst();
    }

    public Set<Task<?>> getAllTasks() {
        return taskMap.keySet();
    }

    private void attachTaskMonitor(Task<?> task) {
        task.stateProperty().addListener(this::taskListener);
    }

    private void detachTaskMonitor(Task<?> task) {
        task.stateProperty().removeListener(this::taskListener);
    }

    private void taskListener(ObservableValue<? extends Worker.State> stateProperty, Worker.State oldState, Worker.State newState) {

        @SuppressWarnings("unchecked")
        Property<Worker.State> property = (Property<Worker.State>) stateProperty;
        Task<?> task = (Task<?>) property.getBean();

        String taskName = task.getClass().getSimpleName();

        switch (newState) {
            case READY:
                log.debug("TASK_READY :: {}", taskName);
                break;
            case SCHEDULED:
                log.debug("TASK_SCHEDULED :: {}", taskName);
                break;
            case RUNNING:
                log.debug("TASK_RUNNING :: {}", taskName);
                break;
            case CANCELLED:
                try {
                    log.warn("TASK_CANCELLED :: {}", taskName);
                    if (taskMap.containsKey(task)) {
                        log.debug("Cancelling completable future of task");
                        CompletableFuture<?> cf = taskMap.get(task);
                        cf.cancel(true);
                        //taskMap.remove(task);
                    } else {
                        log.warn("Unable to find task in map");
                    }
                } finally {
                    detachTaskMonitor(task);
                }
                break;
            case FAILED:
                try {
                    log.debug("TASK_FAILED :: {}", taskName);
                    Throwable err = task.getException();
                    log.error("Exception occured during task run", err);
                    if (taskMap.containsKey(task)) {
                        CompletableFuture<?> cf = taskMap.get(task);
                        cf.completeExceptionally(err);
                        taskMap.remove(task);
                    }
                } finally {
                    detachTaskMonitor(task);
                }
                break;
            case SUCCEEDED:
                try {
                    if (taskMap.containsKey(task)) {
                        log.debug("TASK_SUCCESS :: {}", taskName);
                        //noinspection unchecked
                        CompletableFuture<Object> cf = (CompletableFuture<Object>) taskMap.get(task);
                        cf.complete(task.getValue());
                        taskMap.remove(task);
                    } else {
                        log.warn("TASK_SUCCESS but could not find task in map: {}", task);
                    }
                } finally {
                    detachTaskMonitor(task);
                }
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

    public int getRunningTasks() {
        return runningTasks.get();
    }

    public IntegerProperty runningTasksProperty() {
        return runningTasks;
    }

    public void setRunningTasks(int runningTasks) {
        this.runningTasks.set(runningTasks);
    }

    @Autowired
    public void setSpringHelper(SpringHelper springHelper) {
        this.springHelper = springHelper;
    }
}
