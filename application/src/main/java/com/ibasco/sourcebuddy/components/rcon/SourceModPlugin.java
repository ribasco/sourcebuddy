package com.ibasco.sourcebuddy.components.rcon;

import com.ibasco.sourcebuddy.domain.ManagedServer;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDateTime;
import java.util.Objects;

public class SourceModPlugin {

    private IntegerProperty index = new SimpleIntegerProperty();

    private StringProperty name = new SimpleStringProperty();

    private StringProperty filename = new SimpleStringProperty();

    private StringProperty fullName = new SimpleStringProperty();

    private StringProperty version = new SimpleStringProperty();

    private StringProperty author = new SimpleStringProperty();

    private BooleanProperty disabled = new SimpleBooleanProperty();

    private StringProperty hash = new SimpleStringProperty();

    private StringProperty url = new SimpleStringProperty();

    private StringProperty status = new SimpleStringProperty();

    private ObjectProperty<LocalDateTime> timestamp = new SimpleObjectProperty<>();

    private ObjectProperty<ManagedServer> server = new SimpleObjectProperty<>();

    private ListProperty<SourceModCvar> cvars = new SimpleListProperty<>(FXCollections.observableArrayList());

    private ListProperty<SourceModCommand> commands = new SimpleListProperty<>(FXCollections.observableArrayList());

    private BooleanProperty updating = new SimpleBooleanProperty();

    public int getIndex() {
        return index.get();
    }

    public IntegerProperty indexProperty() {
        return index;
    }

    public void setIndex(int index) {
        this.index.set(index);
    }

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public String getFilename() {
        return filename.get();
    }

    public StringProperty filenameProperty() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename.set(filename);
    }

    public String getFullName() {
        return fullName.get();
    }

    public StringProperty fullNameProperty() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName.set(fullName);
    }

    public String getVersion() {
        return version.get();
    }

    public StringProperty versionProperty() {
        return version;
    }

    public void setVersion(String version) {
        this.version.set(version);
    }

    public String getAuthor() {
        return author.get();
    }

    public StringProperty authorProperty() {
        return author;
    }

    public void setAuthor(String author) {
        this.author.set(author);
    }

    public boolean isDisabled() {
        return disabled.get();
    }

    public BooleanProperty disabledProperty() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled.set(disabled);
    }

    public String getHash() {
        return hash.get();
    }

    public StringProperty hashProperty() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash.set(hash);
    }

    public String getUrl() {
        return url.get();
    }

    public StringProperty urlProperty() {
        return url;
    }

    public void setUrl(String url) {
        this.url.set(url);
    }

    public String getStatus() {
        return status.get();
    }

    public StringProperty statusProperty() {
        return status;
    }

    public void setStatus(String status) {
        this.status.set(status);
    }

    public LocalDateTime getTimestamp() {
        return timestamp.get();
    }

    public ObjectProperty<LocalDateTime> timestampProperty() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp.set(timestamp);
    }

    public ManagedServer getServer() {
        return server.get();
    }

    public ObjectProperty<ManagedServer> serverProperty() {
        return server;
    }

    public void setServer(ManagedServer server) {
        this.server.set(server);
    }

    public ObservableList<SourceModCvar> getCvars() {
        return cvars.get();
    }

    public ListProperty<SourceModCvar> cvarsProperty() {
        return cvars;
    }

    public void setCvars(ObservableList<SourceModCvar> cvars) {
        this.cvars.set(cvars);
    }

    public ObservableList<SourceModCommand> getCommands() {
        return commands.get();
    }

    public ListProperty<SourceModCommand> commandsProperty() {
        return commands;
    }

    public void setCommands(ObservableList<SourceModCommand> commands) {
        this.commands.set(commands);
    }

    public boolean isUpdating() {
        return updating.get();
    }

    public BooleanProperty updatingProperty() {
        return updating;
    }

    public void setUpdating(boolean updating) {
        this.updating.set(updating);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SourceModPlugin plugin = (SourceModPlugin) o;
        return getIndex() == plugin.getIndex() &&
                getServer().equals(plugin.getServer());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getIndex(), getServer());
    }

    @Override
    public String toString() {
        return "SourceModPlugin{" +
                "index=" + getIndex() +
                ", name='" + getName() + '\'' +
                ", filename='" + getFilename() + '\'' +
                ", fullName='" + getFullName() + '\'' +
                ", version='" + getVersion() + '\'' +
                ", author='" + getAuthor() + '\'' +
                ", disabled=" + isDisabled() +
                ", hash='" + getHash() + '\'' +
                ", url='" + getUrl() + '\'' +
                ", status='" + getStatus() + '\'' +
                ", timestamp=" + getTimestamp() +
                '}';
    }
}
