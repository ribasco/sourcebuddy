package com.ibasco.sourcebuddy.model;

import com.ibasco.sourcebuddy.domain.ConfigProfile;
import com.ibasco.sourcebuddy.domain.ServerDetails;
import com.ibasco.sourcebuddy.service.ConfigService;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Component
public class ServerDetailsModel {

    private static final Logger log = LoggerFactory.getLogger(ServerDetailsModel.class);

    private static final ReadWriteLock serverListLock = new ReentrantReadWriteLock();

    public static final Lock WRITE_LOCK = serverListLock.writeLock();

    public static final Lock READ_LOCK = serverListLock.readLock();

    private ConfigService configService;

    private ObjectProperty<ConfigProfile> activeProfile = new SimpleObjectProperty<>();

    private BooleanProperty serverListUpdating = new SimpleBooleanProperty();

    private BooleanProperty serverDetailsUpdating = new SimpleBooleanProperty();

    private ListProperty<ServerDetails> selectedServers = new SimpleListProperty<>(FXCollections.observableArrayList());

    private ObjectProperty<ServerDetails> selectedServer = new SimpleObjectProperty<>();

    private ListProperty<ServerDetails> serverDetails = new SimpleListProperty<>();

    private StringProperty statusMessage = new SimpleStringProperty();

    private ObjectProperty<TreeDataModel<ServerDetails>> managedServers = new SimpleObjectProperty<>();

    @PostConstruct
    void init() {
        ConfigProfile defaultProfile = configService.getDefaultProfile();
        if (defaultProfile == null) {
            log.debug("No default profile assigned. Creating new default profile");
            defaultProfile = configService.saveProfile(configService.createProfile());
            configService.setDefaultProfile(defaultProfile);
            log.debug("Saved default profile: {}", defaultProfile);
        }
        log.debug("Setting active profile: {}", defaultProfile);
        setActiveProfile(defaultProfile);
    }

    public ObservableList<ServerDetails> getServerDetails() {
        try {
            READ_LOCK.lock();
            return serverDetails.get();
        } finally {
            READ_LOCK.unlock();
        }
    }

    public void setServerDetails(ObservableList<ServerDetails> serverDetails) {
        try {
            WRITE_LOCK.lock();
            this.serverDetails.set(serverDetails);
        } finally {
            WRITE_LOCK.unlock();
        }
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

    public boolean isServerListUpdating() {
        return serverListUpdating.get();
    }

    public BooleanProperty serverListUpdatingProperty() {
        return serverListUpdating;
    }

    public void setServerListUpdating(boolean serverListUpdating) {
        this.serverListUpdating.set(serverListUpdating);
    }

    public boolean isServerDetailsUpdating() {
        return serverDetailsUpdating.get();
    }

    public BooleanProperty serverDetailsUpdatingProperty() {
        return serverDetailsUpdating;
    }

    public void setServerDetailsUpdating(boolean serverDetailsUpdating) {
        this.serverDetailsUpdating.set(serverDetailsUpdating);
    }

    public ObservableList<ServerDetails> getSelectedServers() {
        return selectedServers.get();
    }

    public ListProperty<ServerDetails> selectedServersProperty() {
        return selectedServers;
    }

    public void setSelectedServers(ObservableList<ServerDetails> selectedServers) {
        this.selectedServers.set(selectedServers);
    }

    public ServerDetails getSelectedServer() {
        return selectedServer.get();
    }

    public ObjectProperty<ServerDetails> selectedServerProperty() {
        return selectedServer;
    }

    public void setSelectedServer(ServerDetails selectedServer) {
        this.selectedServer.set(selectedServer);
    }

    public TreeDataModel<ServerDetails> getManagedServers() {
        return managedServers.get();
    }

    public ObjectProperty<TreeDataModel<ServerDetails>> managedServersProperty() {
        return managedServers;
    }

    public void setManagedServers(TreeDataModel<ServerDetails> managedServers) {
        this.managedServers.set(managedServers);
    }

    public ConfigProfile getActiveProfile() {
        return activeProfile.get();
    }

    public ObjectProperty<ConfigProfile> activeProfileProperty() {
        return activeProfile;
    }

    public void setActiveProfile(ConfigProfile activeProfile) {
        this.activeProfile.set(activeProfile);
    }

    @Autowired
    void setConfigService(ConfigService configService) {
        this.configService = configService;
    }
}
