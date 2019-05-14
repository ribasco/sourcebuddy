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

    public static final String SHOW_SETTINGS_PANE = "show_settings_pane";

    private IntegerProperty id = new SimpleIntegerProperty();

    private StringProperty name = new SimpleStringProperty();

    private ObjectProperty<SteamApp> defaultGame = new SimpleObjectProperty<>();

    private BooleanProperty showGameThumbnails = new SimpleBooleanProperty();

    private List<ManagedServer> managedServers = new ArrayList<>();

    private ObjectProperty<DockLayout> defaultLayout = new SimpleObjectProperty<>();

    private List<DockLayout> dockLayouts = new ArrayList<>();

    private BooleanProperty showSettingsPane = new SimpleBooleanProperty();

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

    @OneToOne
    @JoinColumn(name = DockLayout.ID)
    public DockLayout getDefaultLayout() {
        return defaultLayout.get();
    }

    public ObjectProperty<DockLayout> defaultLayoutProperty() {
        return defaultLayout;
    }

    public void setDefaultLayout(DockLayout defaultLayout) {
        this.defaultLayout.set(defaultLayout);
    }

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "profile", fetch = FetchType.EAGER, orphanRemoval = true)
    public List<DockLayout> getDockLayouts() {
        return dockLayouts;
    }

    public void setDockLayouts(List<DockLayout> dockLayouts) {
        this.dockLayouts = dockLayouts;
    }

    @Column(name = SHOW_SETTINGS_PANE)
    public Boolean isShowSettingsPane() {
        return showSettingsPane.getValue();
    }

    public BooleanProperty showSettingsPaneProperty() {
        return showSettingsPane;
    }

    public void setShowSettingsPane(Boolean showSettingsPane) {
        this.showSettingsPane.setValue(showSettingsPane);
    }

    @Override
    public String toString() {
        return getName();
    }
}
