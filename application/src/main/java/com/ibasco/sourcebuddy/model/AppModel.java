package com.ibasco.sourcebuddy.model;

import com.ibasco.sourcebuddy.domain.ConfigProfile;
import com.ibasco.sourcebuddy.domain.DockLayout;
import com.ibasco.sourcebuddy.domain.ManagedServer;
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

@Component
public class AppModel {

    private static final Logger log = LoggerFactory.getLogger(AppModel.class);

    private ConfigService configService;

    private ObjectProperty<DockLayout> activeLayout = new SimpleObjectProperty<>();

    private ObjectProperty<ConfigProfile> activeProfile = new SimpleObjectProperty<>();

    private BooleanProperty serverListUpdating = new SimpleBooleanProperty();

    private BooleanProperty serverDetailsUpdating = new SimpleBooleanProperty();

    private ListProperty<ServerDetails> selectedServers = new SimpleListProperty<>(FXCollections.observableArrayList());

    private ObjectProperty<ServerDetails> selectedServer = new SimpleObjectProperty<>();

    private ObjectProperty<ManagedServer> selectedManagedServer = new SimpleObjectProperty<>();

    private ListProperty<ServerDetails> serverDetails = new SimpleListProperty<>();

    private StringProperty statusMessage = new SimpleStringProperty();

    private ObjectProperty<TreeDataModel<ServerDetails>> managedServers = new SimpleObjectProperty<>();

    @PostConstruct
    void init() {
        ConfigProfile defaultProfile = configService.getDefaultProfile();
        if (defaultProfile == null)
            throw new IllegalStateException("Default profile not set");
        log.debug("Setting active profile: {}", defaultProfile);
        setActiveProfile(defaultProfile);
    }

    public ObservableList<ServerDetails> getServerDetails() {
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

    public DockLayout getActiveLayout() {
        return activeLayout.get();
    }

    public ObjectProperty<DockLayout> activeLayoutProperty() {
        return activeLayout;
    }

    public void setActiveLayout(DockLayout activeLayout) {
        this.activeLayout.set(activeLayout);
    }

    public ManagedServer getSelectedManagedServer() {
        return selectedManagedServer.get();
    }

    public ObjectProperty<ManagedServer> selectedManagedServerProperty() {
        return selectedManagedServer;
    }

    public void setSelectedManagedServer(ManagedServer selectedManagedServer) {
        this.selectedManagedServer.set(selectedManagedServer);
    }

    @Autowired
    void setConfigService(ConfigService configService) {
        this.configService = configService;
    }
}
