package com.ibasco.sourcebuddy.entities;

import com.ibasco.agql.protocols.valve.source.query.pojos.SourceServer;
import com.ibasco.sourcebuddy.enums.OperatingSystem;
import com.ibasco.sourcebuddy.enums.ServerStatus;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;

import javax.persistence.*;
import java.net.InetSocketAddress;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "SOURCE_SERVER_DETAILS",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"ip_address", "port"})}
)
public class SourceServerDetails {

    private IntegerProperty id = new SimpleIntegerProperty();

    private StringProperty ipAddress = new SimpleStringProperty("");

    private IntegerProperty port = new SimpleIntegerProperty(-1);

    private StringProperty name = new SimpleStringProperty("N/A");

    private IntegerProperty playerCount = new SimpleIntegerProperty(-1);

    private IntegerProperty maxPlayerCount = new SimpleIntegerProperty(-1);

    private StringProperty mapName = new SimpleStringProperty("N/A");

    private StringProperty serverTags = new SimpleStringProperty("N/A");

    private ListProperty<SourcePlayerInfo> players = new SimpleListProperty<>(null);

    private MapProperty<String, String> rules = new SimpleMapProperty<>(null);

    private ObjectProperty<InetSocketAddress> address = new SimpleObjectProperty<>(null);

    private IntegerProperty appId = new SimpleIntegerProperty(-1);

    private ObjectProperty<OperatingSystem> operatingSystem = new SimpleObjectProperty<>();

    private StringProperty gameDirectory = new SimpleStringProperty("N/A");

    private StringProperty description = new SimpleStringProperty("N/A");

    private StringProperty version = new SimpleStringProperty("N/A");

    private LongProperty gameId = new SimpleLongProperty(-1);

    private ObjectProperty<LocalDateTime> lastUpdate = new SimpleObjectProperty<>(LocalDateTime.now());

    private ObjectProperty<ServerStatus> status = new SimpleObjectProperty<>(ServerStatus.NEW);

    public SourceServerDetails() {
        addressProperty().bind(Bindings.createObjectBinding(() -> {
            if (!ipAddress.get().isBlank() && port.get() > 0) {
                return new InetSocketAddress(ipAddress.get(), port.get());
            }
            return null;
        }, ipAddress, port));
    }

    public SourceServerDetails(String ipAddress, int port) {
        setIpAddress(ipAddress);
        setPort(port);
        setAddress(new InetSocketAddress(ipAddress, port));
    }

    public SourceServerDetails(SourceServer server) {
        setAddress(server.getAddress());
        setName(server.getName());
        setServerTags(server.getServerTags());
        setPlayerCount(server.getNumOfPlayers());
        setMaxPlayerCount(server.getMaxPlayers());
        setMapName(server.getMapName());
        setAppId((int) server.getAppId());
        setOperatingSystem(OperatingSystem.valueOf(server.getOperatingSystem()));
        setGameDirectory(server.getGameDirectory());
        setDescription(server.getGameDescription());
        setVersion(server.getGameVersion());
        setGameId(server.getGameId());
    }

    @Column(name = "server_id")
    @Id
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

    @Column(name = "status")
    public ServerStatus getStatus() {
        return status.get();
    }

    public ObjectProperty<ServerStatus> statusProperty() {
        return status;
    }

    public void setStatus(ServerStatus status) {
        this.status.set(status);
    }

    @Column(name = "game_id")
    public Long getGameId() {
        return gameId.getValue();
    }

    public LongProperty gameIdProperty() {
        return gameId;
    }

    public void setGameId(Long gameId) {
        this.gameId.setValue(gameId);
    }

    @Column(name = "version")
    public String getVersion() {
        return version.get();
    }

    public StringProperty versionProperty() {
        return version;
    }

    public void setVersion(String version) {
        this.version.set(version);
    }

    @Column(name = "description")
    public String getDescription() {
        return description.get();
    }

    public StringProperty descriptionProperty() {
        return description;
    }

    public void setDescription(String description) {
        this.description.set(description);
    }

    @Column(name = "directory")
    public String getGameDirectory() {
        return gameDirectory.get();
    }

    public StringProperty gameDirectoryProperty() {
        return gameDirectory;
    }

    public void setGameDirectory(String gameDirectory) {
        this.gameDirectory.set(gameDirectory);
    }

    @Column(name = "os")
    public OperatingSystem getOperatingSystem() {
        return operatingSystem.get();
    }

    public ObjectProperty<OperatingSystem> operatingSystemProperty() {
        return operatingSystem;
    }

    public void setOperatingSystem(OperatingSystem operatingSystem) {
        this.operatingSystem.set(operatingSystem);
    }

    @Column(name = "app_id")
    public Integer getAppId() {
        return appId.get();
    }

    public IntegerProperty appIdProperty() {
        return appId;
    }

    public void setAppId(Integer appId) {
        this.appId.setValue(appId);
    }

    @Transient
    public InetSocketAddress getAddress() {
        return address.get();
    }

    public ObjectProperty<InetSocketAddress> addressProperty() {
        return address;
    }

    public void setAddress(InetSocketAddress address) {
        this.address.set(address);
    }

    @Column(name = "tags")
    public String getServerTags() {
        return serverTags.get();
    }

    public StringProperty serverTagsProperty() {
        return serverTags;
    }

    public void setServerTags(String serverTags) {
        this.serverTags.set(serverTags);
    }

    @Transient
    public String getMapName() {
        return mapName.get();
    }

    public StringProperty mapNameProperty() {
        return mapName;
    }

    public void setMapName(String mapName) {
        this.mapName.set(mapName);
    }

    @Transient
    public int getMaxPlayerCount() {
        return maxPlayerCount.get();
    }

    public IntegerProperty maxPlayerCountProperty() {
        return maxPlayerCount;
    }

    public void setMaxPlayerCount(int maxPlayerCount) {
        this.maxPlayerCount.set(maxPlayerCount);
    }

    @Column(name = "ip_address")
    public String getIpAddress() {
        return ipAddress.get();
    }

    public StringProperty ipAddressProperty() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress.set(ipAddress);
    }

    @Column(name = "port")
    public int getPort() {
        return port.get();
    }

    public IntegerProperty portProperty() {
        return port;
    }

    public void setPort(int port) {
        this.port.set(port);
    }

    @Column(name = "name")
    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    @Transient
    public int getPlayerCount() {
        return playerCount.get();
    }

    public IntegerProperty playerCountProperty() {
        return playerCount;
    }

    public void setPlayerCount(int playerCount) {
        this.playerCount.set(playerCount);
    }

    @Column(name = "last_update", columnDefinition = "DATETIME")
    public LocalDateTime getLastUpdate() {
        return lastUpdate.get();
    }

    public ObjectProperty<LocalDateTime> lastUpdateProperty() {
        return lastUpdate;
    }

    public void setLastUpdate(LocalDateTime lastUpdate) {
        this.lastUpdate.set(lastUpdate);
    }

    @Transient
    public ObservableList<SourcePlayerInfo> getPlayers() {
        return players.get();
    }

    public ListProperty<SourcePlayerInfo> playersProperty() {
        return players;
    }

    public void setPlayers(ObservableList<SourcePlayerInfo> players) {
        this.players.set(players);
    }

    @Transient
    public ObservableMap<String, String> getRules() {
        return rules.get();
    }

    public MapProperty<String, String> rulesProperty() {
        return rules;
    }

    public void setRules(ObservableMap<String, String> rules) {
        this.rules.set(rules);
    }
}
