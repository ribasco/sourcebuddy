package com.ibasco.sourcebuddy.model;

import com.ibasco.sourcebuddy.domain.ServerDetails;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableSelectionModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Component
public class ServerDetailsModel {

    private static final Logger log = LoggerFactory.getLogger(ServerDetailsModel.class);

    private final ReadWriteLock serverListLock = new ReentrantReadWriteLock();

    public final Lock WRITE_LOCK = serverListLock.writeLock();

    public final Lock READ_LOCK = serverListLock.readLock();

    private ObjectProperty<TableSelectionModel<ServerDetails>> serverSelectionModel = new SimpleObjectProperty<>();

    private ListProperty<ServerDetails> serverDetails = new SimpleListProperty<>(FXCollections.observableArrayList());

    private StringProperty statusMessage = new SimpleStringProperty();

    public ObservableList<ServerDetails> getServerDetails() {
        log.debug("Getting details: {}", serverDetails.get());
        return serverDetails.get();
    }

    public void setServerDetails(ObservableList<ServerDetails> serverDetails) {
        this.serverDetails.set(serverDetails);
    }

    public ListProperty<ServerDetails> serverDetailsProperty() {
        return serverDetails;
    }

    public String getStatusMessage() {
        return statusMessage.get();
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage.set(statusMessage);
    }

    public StringProperty statusMessageProperty() {
        return statusMessage;
    }

    public TableSelectionModel<ServerDetails> getServerSelectionModel() {
        return serverSelectionModel.get();
    }

    public void setServerSelectionModel(TableSelectionModel<ServerDetails> serverSelectionModel) {
        this.serverSelectionModel.set(serverSelectionModel);
    }

    public ObjectProperty<TableSelectionModel<ServerDetails>> serverSelectionModelProperty() {
        return serverSelectionModel;
    }
}
