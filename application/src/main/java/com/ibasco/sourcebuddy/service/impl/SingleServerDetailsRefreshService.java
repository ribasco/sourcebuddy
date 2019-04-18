package com.ibasco.sourcebuddy.service.impl;

import com.ibasco.sourcebuddy.domain.ServerDetails;
import com.ibasco.sourcebuddy.service.ListenableTaskService;
import com.ibasco.sourcebuddy.tasks.UpdateSingleServerDetailsTask;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SingleServerDetailsRefreshService extends ListenableTaskService<Void> {

    private static final Logger log = LoggerFactory.getLogger(SingleServerDetailsRefreshService.class);

    private ObjectProperty<ServerDetails> serverDetails = new SimpleObjectProperty<>();

    @Override
    protected void initialize() {
        setDelay(Duration.ZERO);
        setPeriod(Duration.millis(1500));
    }

    @Override
    protected Task<Void> createNewTask() {
        return getApplicationContext().getBean(UpdateSingleServerDetailsTask.class, serverDetails.get());
    }

    public ServerDetails getServerDetails() {
        return serverDetails.get();
    }

    public ObjectProperty<ServerDetails> serverDetailsProperty() {
        return serverDetails;
    }

    public void setServerDetails(ServerDetails serverDetails) {
        this.serverDetails.set(serverDetails);
    }
}
