package com.ibasco.sourcebuddy.domain;

import javafx.beans.property.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = SteamApp.TABLE_NAME)
public class SteamApp extends AuditableEntity<SteamApp> implements Serializable {

    public static final String TABLE_NAME = "SB_STEAM_APP";

    public static final String APP_ID = "app_id";

    public static final String NAME = "name";

    private static final long serialVersionUID = 7154581209265575164L;

    private IntegerProperty id = new SimpleIntegerProperty();

    private StringProperty name = new SimpleStringProperty();

    private ObjectProperty<SteamAppDetails> appDetails = new SimpleObjectProperty<>();

    public SteamApp() {
    }

    public SteamApp(com.ibasco.agql.protocols.valve.steam.webapi.pojos.SteamApp steamApp) {
        setId(steamApp.getAppid());
        setName(steamApp.getName());
    }

    public IntegerProperty idProperty() {
        return id;
    }

    public StringProperty nameProperty() {
        return name;
    }

    @OneToOne(mappedBy = "steamApp")
    public SteamAppDetails getAppDetails() {
        return appDetails.get();
    }

    public void setAppDetails(SteamAppDetails appDetails) {
        this.appDetails.set(appDetails);
    }

    public ObjectProperty<SteamAppDetails> appDetailsProperty() {
        return appDetails;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SteamApp steamApp = (SteamApp) o;
        return getId().equals(steamApp.getId());
    }

    @Id
    @Column(name = APP_ID)
    public Integer getId() {
        return id.get();
    }

    public void setId(Integer id) {
        this.id.set(id);
    }

    @Override
    public String toString() {
        //return new ReflectionToStringBuilder(this, ToStringStyle.SIMPLE_STYLE).toString();
        return getId() + " :: " + getName();
    }

    @Column(name = NAME)
    public String getName() {
        return name.get();
    }

    public void setName(String name) {
        this.name.set(name);
    }
}
