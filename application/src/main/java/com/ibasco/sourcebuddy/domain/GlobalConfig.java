package com.ibasco.sourcebuddy.domain;

import static com.ibasco.sourcebuddy.domain.GlobalConfig.TABLE_NAME;
import javafx.beans.property.*;

import javax.persistence.*;

@Entity
@Table(name = TABLE_NAME)
public class GlobalConfig extends AuditableEntity<String> {

    public static final String TABLE_NAME = "SB_CONFIG_GLOBAL";

    public static final String ID = "config_id";

    private IntegerProperty id = new SimpleIntegerProperty();

    private StringProperty name = new SimpleStringProperty();

    private ObjectProperty<SteamApp> defaultGame = new SimpleObjectProperty<>();

    private BooleanProperty showGameThumbnails = new SimpleBooleanProperty();

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

    public boolean isShowGameThumbnails() {
        return showGameThumbnails.get();
    }

    public BooleanProperty showGameThumbnailsProperty() {
        return showGameThumbnails;
    }

    public void setShowGameThumbnails(boolean showGameThumbnails) {
        this.showGameThumbnails.set(showGameThumbnails);
    }
}
