package com.ibasco.sourcebuddy.domain;

import com.ibasco.agql.protocols.valve.source.query.pojos.SourceServer;
import com.ibasco.sourcebuddy.enums.OperatingSystem;
import com.ibasco.sourcebuddy.enums.ServerStatus;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;

import javax.persistence.*;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.util.Objects;

@Entity
@Table(
        name = ServerDetails.TABLE_NAME,
        uniqueConstraints = {@UniqueConstraint(columnNames = {"ip_address", "port"})},
        indexes = {@Index(name = "IDX_UNQ_IPPORT", columnList = "ip_address,port")}
)
public class ServerDetails extends AuditableEntity<ServerDetails> implements Serializable {

    public static final String TABLE_NAME = "SB_SERVER_DETAILS";

    public static final String SERVER_ID = "server_id";

    public static final String STATUS = "status";

    public static final String GAME_ID = "game_id";

    public static final String VERSION = "version";

    public static final String DESCRIPTION = "description";

    public static final String DIRECTORY = "directory";

    public static final String OS = "os";

    public static final String APP_ID = "app_id";

    public static final String TAGS = "tags";

    public static final String IP_ADDRESS = "ip_address";

    public static final String PORT = "port";

    public static final String NAME = "name";

    public static final String BOOKMARKED = "bookmarked";

    private static final String STEAM_ID = "steam_id";

    private static final String DEDICATED = "dedicated";

    private static final String SECURE = "secure";

    private IntegerProperty id = new SimpleIntegerProperty();

    private StringProperty ipAddress = new SimpleStringProperty("");

    private IntegerProperty port = new SimpleIntegerProperty(-1);

    private StringProperty name = new SimpleStringProperty();

    private IntegerProperty playerCount = new SimpleIntegerProperty(-1);

    private IntegerProperty maxPlayerCount = new SimpleIntegerProperty(-1);

    private StringProperty mapName = new SimpleStringProperty();

    private StringProperty serverTags = new SimpleStringProperty();

    private ListProperty<PlayerInfo> players = new SimpleListProperty<>();

    private MapProperty<String, String> rules = new SimpleMapProperty<>();

    private ObjectProperty<InetSocketAddress> address = new SimpleObjectProperty<>();

    private ObjectProperty<SteamApp> steamApp = new SimpleObjectProperty<>();

    private ObjectProperty<OperatingSystem> operatingSystem = new SimpleObjectProperty<>();

    private StringProperty gameDirectory = new SimpleStringProperty();

    private StringProperty description = new SimpleStringProperty();

    private StringProperty version = new SimpleStringProperty();

    private LongProperty gameId = new SimpleLongProperty(-1);

    private ObjectProperty<ServerStatus> status = new SimpleObjectProperty<>(ServerStatus.NEW);

    private ObjectProperty<Country> country = new SimpleObjectProperty<>();

    private BooleanProperty bookmarked = new SimpleBooleanProperty();

    private LongProperty steamId = new SimpleLongProperty();

    private BooleanProperty dedicated = new SimpleBooleanProperty();

    private BooleanProperty secure = new SimpleBooleanProperty();

    public ServerDetails() {
        addressProperty().bind(Bindings.createObjectBinding(() -> {
            if (!ipAddress.get().isBlank() && port.get() > 0) {
                return new InetSocketAddress(ipAddress.get(), port.get());
            }
            return null;
        }, ipAddress, port));
    }

    public ObjectProperty<InetSocketAddress> addressProperty() {
        return address;
    }

    public ServerDetails(InetSocketAddress address) {
        setIpAddress(address.getAddress().getHostAddress());
        setPort(address.getPort());
        setAddress(address);
    }

    public ServerDetails(String ipAddress, int port) {
        setIpAddress(ipAddress);
        setPort(port);
        setAddress(new InetSocketAddress(ipAddress, port));
    }

    public ServerDetails(SourceServer server) {
        setAddress(server.getAddress());
        setName(server.getName());
        setServerTags(server.getServerTags());
        setPlayerCount(server.getNumOfPlayers());
        setMaxPlayerCount(server.getMaxPlayers());
        setMapName(server.getMapName());
        setOperatingSystem(OperatingSystem.valueOf(server.getOperatingSystem()));
        setGameDirectory(server.getGameDirectory());
        setDescription(server.getGameDescription());
        setVersion(server.getGameVersion());
        setGameId(server.getGameId());
    }

    @Column(name = SERVER_ID)
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

    //@OneToOne(cascade = CascadeType.MERGE, mappedBy = "")
    /*@JoinColumn(name = SteamApp.APP_ID, referencedColumnName = SteamApp.APP_ID)*/
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "app_id", nullable = true)
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
    public int getMaxPlayerCount() {
        return maxPlayerCount.get();
    }

    public void setMaxPlayerCount(int maxPlayerCount) {
        this.maxPlayerCount.set(maxPlayerCount);
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
    public int getPlayerCount() {
        return playerCount.get();
    }

    public void setPlayerCount(int playerCount) {
        this.playerCount.set(playerCount);
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
    public int getPort() {
        return port.get();
    }

    public void setPort(int port) {
        this.port.set(port);
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
        return getIpAddress().equals(that.getIpAddress()) && getPort() == that.getPort();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getIpAddress(), getPort());
    }
}
