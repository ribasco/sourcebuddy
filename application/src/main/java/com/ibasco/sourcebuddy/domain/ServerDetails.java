package com.ibasco.sourcebuddy.domain;

import com.ibasco.agql.protocols.valve.source.query.pojos.SourceServer;
import com.ibasco.sourcebuddy.enums.OperatingSystem;
import com.ibasco.sourcebuddy.enums.ServerStatus;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;

import javax.persistence.*;
import java.net.InetSocketAddress;
import java.util.Objects;

@Entity
@Table(name = ServerDetails.TABLE_NAME)
public class ServerDetails extends AuditableEntity<String> {

    static final String TABLE_NAME = "SB_SERVER_DETAILS";

    static final String ID = "server_id";

    static final String STATUS = "status";

    static final String GAME_ID = "game_id";

    static final String VERSION = "version";

    static final String DESCRIPTION = "description";

    static final String DIRECTORY = "directory";

    static final String OS = "os";

    static final String APP_ID = "app_id";

    static final String TAGS = "tags";

    static final String IP_ADDRESS = "ip_address";

    static final String PORT = "port";

    static final String NAME = "name";

    static final String BOOKMARKED = "bookmarked";

    static final String STEAM_ID = "steam_id";

    static final String DEDICATED = "dedicated";

    static final String SECURE = "secure";

    private IntegerProperty id = new SimpleIntegerProperty();

    private StringProperty ipAddress = new SimpleStringProperty();

    private IntegerProperty port = new SimpleIntegerProperty();

    private StringProperty name = new SimpleStringProperty();

    private IntegerProperty playerCount = new SimpleIntegerProperty();

    private IntegerProperty maxPlayerCount = new SimpleIntegerProperty();

    private StringProperty mapName = new SimpleStringProperty();

    private StringProperty serverTags = new SimpleStringProperty();

    private ListProperty<PlayerInfo> players = new SimpleListProperty<>(FXCollections.observableArrayList());

    private MapProperty<String, String> rules = new SimpleMapProperty<>(FXCollections.observableHashMap());

    private ObjectProperty<InetSocketAddress> address = new SimpleObjectProperty<>();

    private ObjectProperty<SteamApp> steamApp = new SimpleObjectProperty<>();

    private ObjectProperty<OperatingSystem> operatingSystem = new SimpleObjectProperty<>();

    private StringProperty gameDirectory = new SimpleStringProperty();

    private StringProperty description = new SimpleStringProperty();

    private StringProperty version = new SimpleStringProperty();

    private LongProperty gameId = new SimpleLongProperty();

    private ObjectProperty<ServerStatus> status = new SimpleObjectProperty<>(ServerStatus.NEW);

    private ObjectProperty<Country> country = new SimpleObjectProperty<>();

    private BooleanProperty bookmarked = new SimpleBooleanProperty();

    private LongProperty steamId = new SimpleLongProperty();

    private BooleanProperty dedicated = new SimpleBooleanProperty();

    private BooleanProperty secure = new SimpleBooleanProperty();

    public ServerDetails() {
    }

    public ServerDetails(String name) {
        setName(name);
    }

    @PostLoad
    private void onLoad() {
        setAddress(new InetSocketAddress(getIpAddress(), getPort()));
    }

    public ServerDetails(InetSocketAddress address) {
        setIpAddress(address.getAddress().getHostAddress());
        setPort(address.getPort());
        setAddress(address);
    }

    public ServerDetails(String ipAddress, Integer port) {
        setIpAddress(ipAddress);
        setPort(port);
        setAddress(new InetSocketAddress(ipAddress, port));
    }

    public ServerDetails(SourceServer server) {
        setAddress(server.getAddress());
        setName(server.getName());
        setServerTags(server.getServerTags());
        setPlayerCount((int) server.getNumOfPlayers());
        setMaxPlayerCount((int) server.getMaxPlayers());
        setMapName(server.getMapName());
        setOperatingSystem(OperatingSystem.valueOf(server.getOperatingSystem()));
        setGameDirectory(server.getGameDirectory());
        setDescription(server.getGameDescription());
        setVersion(server.getGameVersion());
        setGameId(server.getGameId());
    }

    @Column(name = ID)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer getId() {
        return id.getValue();
    }

    public void setId(Integer id) {
        this.id.setValue(id);
    }

    public IntegerProperty idProperty() {
        return id;
    }

    @Column(name = STATUS)
    public ServerStatus getStatus() {
        return status.get();
    }

    public void setStatus(ServerStatus status) {
        this.status.set(status);
    }

    public ObjectProperty<ServerStatus> statusProperty() {
        return status;
    }

    @Column(name = GAME_ID)
    public Long getGameId() {
        return gameId.getValue();
    }

    public void setGameId(Long gameId) {
        this.gameId.setValue(gameId);
    }

    public LongProperty gameIdProperty() {
        return gameId;
    }

    @Column(name = VERSION)
    public String getVersion() {
        return version.get();
    }

    public void setVersion(String version) {
        this.version.set(version);
    }

    public StringProperty versionProperty() {
        return version;
    }

    @Column(name = DESCRIPTION)
    public String getDescription() {
        return description.get();
    }

    public void setDescription(String description) {
        this.description.set(description);
    }

    public StringProperty descriptionProperty() {
        return description;
    }

    @Column(name = DIRECTORY)
    public String getGameDirectory() {
        return gameDirectory.get();
    }

    public void setGameDirectory(String gameDirectory) {
        this.gameDirectory.set(gameDirectory);
    }

    public StringProperty gameDirectoryProperty() {
        return gameDirectory;
    }

    @Column(name = OS)
    public OperatingSystem getOperatingSystem() {
        return operatingSystem.get();
    }

    public void setOperatingSystem(OperatingSystem operatingSystem) {
        this.operatingSystem.set(operatingSystem);
    }

    public ObjectProperty<OperatingSystem> operatingSystemProperty() {
        return operatingSystem;
    }

    @ManyToOne(optional = false)
    @JoinColumn(name = "app_id")
    public SteamApp getSteamApp() {
        return steamApp.get();
    }

    public void setSteamApp(SteamApp steamApp) {
        this.steamApp.set(steamApp);
    }

    public ObjectProperty<SteamApp> steamAppProperty() {
        return steamApp;
    }

    @Transient
    public InetSocketAddress getAddress() {
        return address.get();
    }

    public void setAddress(InetSocketAddress address) {
        this.address.set(address);
    }

    public ObjectProperty<InetSocketAddress> addressProperty() {
        return address;
    }

    @Column(name = TAGS)
    public String getServerTags() {
        return serverTags.get();
    }

    public void setServerTags(String serverTags) {
        this.serverTags.set(serverTags);
    }

    public StringProperty serverTagsProperty() {
        return serverTags;
    }

    @Transient
    public String getMapName() {
        return mapName.get();
    }

    public void setMapName(String mapName) {
        this.mapName.set(mapName);
    }

    public StringProperty mapNameProperty() {
        return mapName;
    }

    @Transient
    public Integer getMaxPlayerCount() {
        return maxPlayerCount.getValue();
    }

    public void setMaxPlayerCount(Integer maxPlayerCount) {
        this.maxPlayerCount.setValue(maxPlayerCount);
    }

    public IntegerProperty maxPlayerCountProperty() {
        return maxPlayerCount;
    }

    public StringProperty ipAddressProperty() {
        return ipAddress;
    }

    public IntegerProperty portProperty() {
        return port;
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

    @Transient
    public Integer getPlayerCount() {
        return playerCount.getValue();
    }

    public void setPlayerCount(Integer playerCount) {
        this.playerCount.setValue(playerCount);
    }

    public IntegerProperty playerCountProperty() {
        return playerCount;
    }

    @Transient
    public ObservableList<PlayerInfo> getPlayers() {
        return players.get();
    }

    public void setPlayers(ObservableList<PlayerInfo> players) {
        this.players.set(players);
    }

    public ListProperty<PlayerInfo> playersProperty() {
        return players;
    }

    @Transient
    public ObservableMap<String, String> getRules() {
        return rules.get();
    }

    public void setRules(ObservableMap<String, String> rules) {
        this.rules.set(rules);
    }

    public MapProperty<String, String> rulesProperty() {
        return rules;
    }

    @ManyToOne
    @JoinColumn(name = Country.COUNTRY_CODE)
    public Country getCountry() {
        return country.get();
    }

    public void setCountry(Country country) {
        this.country.set(country);
    }

    public ObjectProperty<Country> countryProperty() {
        return country;
    }

    @Column(name = PORT)
    public Integer getPort() {
        return port.getValue();
    }

    public void setPort(Integer port) {
        this.port.setValue(port);
    }

    @Column(name = STEAM_ID)
    public Long getSteamId() {
        return steamId.getValue();
    }

    public LongProperty steamIdProperty() {
        return steamId;
    }

    public void setSteamId(Long steamId) {
        this.steamId.setValue(steamId);
    }

    @Column(name = IP_ADDRESS)
    public String getIpAddress() {
        return ipAddress.get();
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress.set(ipAddress);
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

    @Column(name = DEDICATED)
    public boolean isDedicated() {
        return dedicated.getValue();
    }

    public BooleanProperty dedicatedProperty() {
        return dedicated;
    }

    public void setDedicated(Boolean dedicated) {
        this.dedicated.setValue(dedicated);
    }

    @Column(name = SECURE)
    public Boolean isSecure() {
        return secure.getValue();
    }

    public BooleanProperty secureProperty() {
        return secure;
    }

    public void setSecure(Boolean secure) {
        this.secure.setValue(secure);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServerDetails that = (ServerDetails) o;
        if (getIpAddress() == null || that.getIpAddress() == null)
            return false;
        return getIpAddress().equals(that.getIpAddress()) && getPort().equals(that.getPort());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getIpAddress(), getPort());
    }

    @Override
    public String toString() {
        return String.format("%s:%d = %s", getIpAddress(), getPort(), getName());
    }
}
