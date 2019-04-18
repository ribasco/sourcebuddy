package com.ibasco.sourcebuddy.domain;

import javafx.beans.property.*;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = SteamApp.TABLE_NAME)
public class SteamApp extends AuditableEntity<String> implements Comparable<SteamApp> {

    public static final String TABLE_NAME = "SB_STEAM_APP";

    public static final String APP_ID = "app_id";

    public static final String NAME = "name";

    public static final String BOOKMARKED = "bookmarked";

    private static final long serialVersionUID = 7154581209265575164L;

    private IntegerProperty id = new SimpleIntegerProperty();

    private StringProperty name = new SimpleStringProperty();

    private BooleanProperty bookmarked = new SimpleBooleanProperty();

    private BooleanProperty defaultApp = new SimpleBooleanProperty();

    private ObjectProperty<SteamAppDetails> appDetails = new SimpleObjectProperty<>();

    public SteamApp() {
    }

    public SteamApp(com.ibasco.agql.protocols.valve.steam.webapi.pojos.SteamApp steamApp) {
        setId(steamApp.getAppid());
        setName(steamApp.getName());
    }

    @Id
    @Column(name = APP_ID)
    public Integer getId() {
        return id.get();
    }

    public IntegerProperty idProperty() {
        return id;
    }

    public void setId(Integer id) {
        this.id.set(id);
    }

    @Column(name = NAME)
    public String getName() {
        return name.get();
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public StringProperty nameProperty() {
        return name;
    }

    @OneToOne(mappedBy = "steamApp", orphanRemoval = true)
    public SteamAppDetails getAppDetails() {
        return appDetails.get();
    }

    public void setAppDetails(SteamAppDetails appDetails) {
        this.appDetails.set(appDetails);
    }

    public ObjectProperty<SteamAppDetails> appDetailsProperty() {
        return appDetails;
    }

    @Column(name = BOOKMARKED)
    public Boolean isBookmarked() {
        return bookmarked.getValue();
    }

    public BooleanProperty bookmarkedProperty() {
        return bookmarked;
    }

    public void setBookmarked(Boolean bookmarked) {
        this.bookmarked.setValue(bookmarked);
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

    @Override
    public String toString() {
        return getName() + " (" + getId() + ")";
    }

    @Override
    public int compareTo(SteamApp o) {
        if (o.isBookmarked()) {
            return Integer.compare(1, o.isBookmarked() ? 1 : 0);
        }
        return this.getId().compareTo(o.getId());
    }
}
