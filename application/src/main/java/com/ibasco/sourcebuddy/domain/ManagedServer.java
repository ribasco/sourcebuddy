package com.ibasco.sourcebuddy.domain;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;

import javax.persistence.*;

@Entity
@Table(name = ManagedServer.TABLE_NAME)
public class ManagedServer extends AuditableEntity<String> {

    public static final String TABLE_NAME = "SB_MANAGED_SERVERS";

    private IntegerProperty id = new SimpleIntegerProperty();

    private ObjectProperty<ServerDetails> serverDetails = new SimpleObjectProperty<>();

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
    @JoinColumn(name = ServerDetails.SERVER_ID)
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
