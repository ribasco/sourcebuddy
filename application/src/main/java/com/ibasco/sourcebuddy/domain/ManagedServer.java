package com.ibasco.sourcebuddy.domain;

import javafx.beans.property.*;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = ManagedServer.TABLE_NAME, uniqueConstraints = @UniqueConstraint(name = "UIDX_PROFILE_SERVER", columnNames = {ConfigProfile.ID, ServerDetails.ID}))
public class ManagedServer extends AuditableEntity<String> {

    public static final String TABLE_NAME = "SB_MANAGED_SERVERS";

    public static final String RCON_PASSWORD = "rcon_password";

    private IntegerProperty id = new SimpleIntegerProperty();

    private ObjectProperty<ServerDetails> serverDetails = new SimpleObjectProperty<>();

    private ObjectProperty<ConfigProfile> profile = new SimpleObjectProperty<>();

    private StringProperty rconPassword = new SimpleStringProperty();

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

    @Override
    public String toString() {
        return getServerDetails().toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ManagedServer that = (ManagedServer) o;
        return getServerDetails().equals(that.getServerDetails());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getServerDetails());
    }
}
