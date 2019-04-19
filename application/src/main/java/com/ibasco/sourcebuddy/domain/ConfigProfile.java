package com.ibasco.sourcebuddy.domain;

import static com.ibasco.sourcebuddy.domain.ConfigProfile.TABLE_NAME;
import javafx.beans.property.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = TABLE_NAME)
public class ConfigProfile extends AuditableEntity<String> {

    public static final String TABLE_NAME = "SB_CONFIG_PROFILE";

    public static final String ID = "profile_id";

    public static final String NAME = "name";

    public static final String SHOW_GAME_THUMBNAILS = "show_game_thumbnails";

    private IntegerProperty id = new SimpleIntegerProperty();

    private StringProperty name = new SimpleStringProperty();

    private ObjectProperty<SteamApp> defaultGame = new SimpleObjectProperty<>();

    private BooleanProperty showGameThumbnails = new SimpleBooleanProperty();

    private List<ManagedServer> managedServers = new ArrayList<>();

    @Id
    @Column(name = ID)
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

    @Column(name = NAME, unique = true)
    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    @OneToOne
    @JoinColumn(name = SteamApp.APP_ID, unique = true)
    public SteamApp getDefaultGame() {
        return defaultGame.get();
    }

    public ObjectProperty<SteamApp> defaultGameProperty() {
        return defaultGame;
    }

    public void setDefaultGame(SteamApp defaultGame) {
        this.defaultGame.set(defaultGame);
    }

    @Column(name = SHOW_GAME_THUMBNAILS)
    public boolean isShowGameThumbnails() {
        return showGameThumbnails.get();
    }

    public BooleanProperty showGameThumbnailsProperty() {
        return showGameThumbnails;
    }

    public void setShowGameThumbnails(boolean showGameThumbnails) {
        this.showGameThumbnails.set(showGameThumbnails);
    }

    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    public List<ManagedServer> getManagedServers() {
        return managedServers;
    }

    public void setManagedServers(List<ManagedServer> managedServers) {
        this.managedServers = managedServers;
    }
}