package com.ibasco.sourcebuddy.tasks;

import com.ibasco.sourcebuddy.annotations.AbstractComponent;
import com.ibasco.sourcebuddy.domain.ServerDetails;
import com.ibasco.sourcebuddy.util.WorkProgressCallback;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicInteger;

@AbstractComponent
public abstract class BaseTask<T> extends Task<T> {

    private static final Logger log = LoggerFactory.getLogger(BaseTask.class);

    private ObjectProperty<Duration> duration = new SimpleObjectProperty<>();

    protected BaseTask() {

    }

    void updateTitle(String message, Object... args) {
        String formattedMsg = String.format(message, args);
        super.updateTitle(formattedMsg);
    }

    void updateMessage(String message, Object... args) {
        String formattedMsg = String.format(message, args);
        super.updateMessage(formattedMsg);
    }

    abstract protected T process() throws Exception;

    protected void exit() {
    }

    @Override
    protected final T call() throws Exception {
        long startTime = System.currentTimeMillis();
        try {
            return process();
        } catch (Exception ex) {
            if (ex instanceof CompletionException) {
                if (ex.getCause() instanceof CancellationException) {
                    log.warn("Task {} canelled", this);
                    throw (CancellationException) ex.getCause();
                }
            } else {
                log.debug("Exception occured on task: " + this, ex);
            }
            throw ex;
        } finally {
            duration.set(Duration.ofMillis(System.currentTimeMillis() - startTime));
            exit();
        }
    }

    WorkProgressCallback<ServerDetails> createWorkProgressCallback(String workDesc, final int totalWork) {
        return new WorkProgressCallback<>() {
            final AtomicInteger workCtr = new AtomicInteger();

            final AtomicInteger successCtr = new AtomicInteger();

            final AtomicInteger failedCtr = new AtomicInteger();

            {
                if (totalWork < 0)
                    updateProgress(-1, Long.MIN_VALUE);
            }

            @Override
            public void onProgress(ServerDetails item, Throwable ex) {
                if (ex != null) {
                    failedCtr.incrementAndGet();
                } else {
                    successCtr.incrementAndGet();
                }

                final int work = workCtr.incrementAndGet();

                //indeterminate work
                if (totalWork < 0) {
                    String itemString = item != null ? item.getAddress().toString().replace("/", "") : "N/A";
                    updateMessage(
                            "%s (IP: %s, Processed: %d)",
                            workDesc,
                            itemString,
                            work
                    );
                } else {
                    updateProgress(work, totalWork);
                    updateMessage(
                            "%s (Pass: %d, Err: %d, %d/%d (%.2f%%))",
                            workDesc,
                            successCtr.get(),
                            failedCtr.get(),
                            workCtr.get(),
                            totalWork,
                            ((double) work / (double) totalWork) * 100.0d
                    );
                }
            }
        };
    }

    public Duration getDuration() {
        return duration.get();
    }

    public ObjectProperty<Duration> durationProperty() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration.set(duration);
    }

}

