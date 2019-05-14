package com.ibasco.sourcebuddy.domain;

import javafx.beans.property.*;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = ManagedServer.TABLE_NAME, uniqueConstraints = @UniqueConstraint(name = "UIDX_PROFILE_SERVER", columnNames = {ConfigProfile.ID, ServerDetails.ID}))
public class ManagedServer extends AuditableEntity<String> {

    public static final String TABLE_NAME = "SB_MANAGED_SERVERS";

    public static final String RCON_PASSWORD = "rcon_password";

    public static final String LOG_LISTEN_IP = "log_listen_ip";

    public static final String LOG_LISTEN_PORT = "log_listen_port";

    public static final String CONSOLE_BUFFER_SIZE = "console_buffer_size";

    private IntegerProperty id = new SimpleIntegerProperty();

    private ObjectProperty<ServerDetails> serverDetails = new SimpleObjectProperty<>();

    private ObjectProperty<ConfigProfile> profile = new SimpleObjectProperty<>();

    private StringProperty rconPassword = new SimpleStringProperty();

    private StringProperty logListenIp = new SimpleStringProperty();

    private IntegerProperty logListenPort = new SimpleIntegerProperty();

    private IntegerProperty bufferSize = new SimpleIntegerProperty(512000);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "managed_server_id")
    public int getId() {
        return id.get();
    }

    public IntegerProperty idProperty() {
        return id;
    }

    public void setId(int id) {
        this.id.set(id);
    }

    @OneToOne
    @JoinColumn(name = ServerDetails.ID)
    public ServerDetails getServerDetails() {
        return serverDetails.get();
    }

    public ObjectProperty<ServerDetails> serverDetailsProperty() {
        return serverDetails;
    }

    public void setServerDetails(ServerDetails serverDetails) {
        this.serverDetails.set(serverDetails);
    }

    @ManyToOne
    @JoinColumn(name = ConfigProfile.ID)
    public ConfigProfile getProfile() {
        return profile.get();
    }

    public ObjectProperty<ConfigProfile> profileProperty() {
        return profile;
    }

    public void setProfile(ConfigProfile profile) {
        this.profile.set(profile);
    }

    @Column(name = RCON_PASSWORD)
    public String getRconPassword() {
        return rconPassword.get();
    }

    public StringProperty rconPasswordProperty() {
        return rconPassword;
    }

    public void setRconPassword(String rconPassword) {
        this.rconPassword.set(rconPassword);
    }

    @Column(name = LOG_LISTEN_IP)
    public String getLogListenIp() {
        return logListenIp.get();
    }

    public StringProperty logListenIpProperty() {
        return logListenIp;
    }

    public void setLogListenIp(String logListenIp) {
        this.logListenIp.set(logListenIp);
    }

    @Column(name = LOG_LISTEN_PORT)
    public Integer getLogListenPort() {
        return logListenPort.getValue();
    }

    public IntegerProperty logListenPortProperty() {
        return logListenPort;
    }

    public void setLogListenPort(Integer logListenPort) {
        this.logListenPort.setValue(logListenPort);
    }

    @Column(name = CONSOLE_BUFFER_SIZE)
    public Integer getBufferSize() {
        return bufferSize.getValue();
    }

    public IntegerProperty bufferSizeProperty() {
        return bufferSize;
    }

    public void setBufferSize(Integer bufferSize) {
        this.bufferSize.setValue(bufferSize);
    }

    @Override
    public String toString() {
        return getServerDetails().toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ManagedServer that = (ManagedServer) o;
        if (getServerDetails() == null || that.getServerDetails() == null) {
            return false;
        }
        if (getServerDetails() == ((ManagedServer) o).getServerDetails())
            return true;
        return getServerDetails().equals(that.getServerDetails());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getServerDetails());
    }
}
