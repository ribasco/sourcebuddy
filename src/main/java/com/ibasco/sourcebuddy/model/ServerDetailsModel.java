package com.ibasco.sourcebuddy.model;

import com.ibasco.sourcebuddy.entities.SourceServerDetails;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableSelectionModel;
import org.springframework.stereotype.Component;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Component
public class ServerDetailsModel {
    private final ReadWriteLock serverListLock = new ReentrantReadWriteLock();

    public final Lock writeLock = serverListLock.writeLock();

    public final Lock readLock = serverListLock.readLock();

    private ObjectProperty<TableSelectionModel<SourceServerDetails>> serverSelectionModel = new SimpleObjectProperty<>();

    private ListProperty<SourceServerDetails> serverDetails = new SimpleListProperty<>(FXCollections.observableArrayList());

    private StringProperty statusMessage = new SimpleStringProperty();

    public ObservableList<SourceServerDetails> getServerDetails() {
        return serverDetails.get();
    }

    public ListProperty<SourceServerDetails> serverDetailsProperty() {
        return serverDetails;
    }

    public void setServerDetails(ObservableList<SourceServerDetails> serverDetails) {
        this.serverDetails.set(serverDetails);
    }

    public String getStatusMessage() {
        return statusMessage.get();
    }

    public StringProperty statusMessageProperty() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage.set(statusMessage);
    }

    public TableSelectionModel<SourceServerDetails> getServerSelectionModel() {
        return serverSelectionModel.get();
    }

    public ObjectProperty<TableSelectionModel<SourceServerDetails>> serverSelectionModelProperty() {
        return serverSelectionModel;
    }

    public void setServerSelectionModel(TableSelectionModel<SourceServerDetails> serverSelectionModel) {
        this.serverSelectionModel.set(serverSelectionModel);
    }
}
