package com.ibasco.sourcebuddy.domain;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;

import javax.persistence.*;

@Entity
@Table(name = ManagedGame.TABLE_NAME)
public class ManagedGame extends AuditableEntity<String> {

    public static final String TABLE_NAME = "SB_MANAGED_GAMES";

    public static final String MANAGED_GAME_ID = "managed_game_id";

    private IntegerProperty id = new SimpleIntegerProperty();

    private ObjectProperty<SteamApp> steamApp = new SimpleObjectProperty<>();

    @Id
    @Column(name = MANAGED_GAME_ID)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
    @JoinColumn(name = SteamApp.APP_ID)
    public SteamApp getSteamApp() {
        return steamApp.get();
    }

    public ObjectProperty<SteamApp> steamAppProperty() {
        return steamApp;
    }

    public void setSteamApp(SteamApp steamApp) {
        this.steamApp.set(steamApp);
    }
}
