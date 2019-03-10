package com.ibasco.sourcebuddy.sourcebuddy.model;

import com.ibasco.agql.protocols.valve.source.query.pojos.SourceServer;
import javafx.beans.property.*;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;

import java.net.InetSocketAddress;

public class SourceServerInfo {
    private StringProperty ipAddress = new SimpleStringProperty("0.0.0.0");

    private StringProperty name = new SimpleStringProperty("N/A");

    private IntegerProperty playerCount = new SimpleIntegerProperty(0);

    private IntegerProperty maxPlayerCount = new SimpleIntegerProperty(8);

    private StringProperty mapName = new SimpleStringProperty("N/A");

    private StringProperty serverTags = new SimpleStringProperty("N/A");

    private ListProperty<SourcePlayerInfo> players = new SimpleListProperty<>(null);

    private MapProperty<String, String> rules = new SimpleMapProperty<>(null);

    private ObjectProperty<InetSocketAddress> address = new SimpleObjectProperty<>(null);

    private IntegerProperty appId = new SimpleIntegerProperty(-1);

    private IntegerProperty operatingSystem = new SimpleIntegerProperty(-1);

    private StringProperty gameDirectory = new SimpleStringProperty("N/A");

    private StringProperty description = new SimpleStringProperty("N/A");

    private StringProperty version = new SimpleStringProperty("N/A");

    private LongProperty gameId = new SimpleLongProperty(-1);

    private LongProperty lastUpdate = new SimpleLongProperty(0);

    public SourceServerInfo(SourceServer server) {
        setPlayers(null);
        setRules(null);
        setAddress(server.getAddress());
        setName(server.getName());
        setServerTags(server.getServerTags());
        setPlayerCount(server.getNumOfPlayers());
        setMaxPlayerCount(server.getMaxPlayers());
        setMapName(server.getMapName());
        setAppId(server.getAppId());
        setOperatingSystem(server.getOperatingSystem());
        setGameDirectory(server.getGameDirectory());
        setDescription(server.getGameDescription());
        setVersion(server.getGameVersion());
        setGameId(server.getGameId());
        setLastUpdate(System.currentTimeMillis());
    }

    public ObservableMap<String, String> getRules() {
        return rules.get();
    }

    public MapProperty<String, String> rulesProperty() {
        return rules;
    }

    public void setRules(ObservableMap<String, String> rules) {
        this.rules.set(rules);
    }

    public long getGameId() {
        return gameId.get();
    }

    public LongProperty gameIdProperty() {
        return gameId;
    }

    public void setGameId(long gameId) {
        this.gameId.set(gameId);
    }

    public String getVersion() {
        return version.get();
    }

    public StringProperty versionProperty() {
        return version;
    }

    public void setVersion(String version) {
        this.version.set(version);
    }

    public String getDescription() {
        return description.get();
    }

    public StringProperty descriptionProperty() {
        return description;
    }

    public void setDescription(String description) {
        this.description.set(description);
    }

    public String getGameDirectory() {
        return gameDirectory.get();
    }

    public StringProperty gameDirectoryProperty() {
        return gameDirectory;
    }

    public void setGameDirectory(String gameDirectory) {
        this.gameDirectory.set(gameDirectory);
    }

    public int getOperatingSystem() {
        return operatingSystem.get();
    }

    public IntegerProperty operatingSystemProperty() {
        return operatingSystem;
    }

    public void setOperatingSystem(int operatingSystem) {
        this.operatingSystem.set(operatingSystem);
    }

    public int getAppId() {
        return appId.get();
    }

    public IntegerProperty appIdProperty() {
        return appId;
    }

    public void setAppId(int appId) {
        this.appId.set(appId);
    }

    public InetSocketAddress getAddress() {
        return address.get();
    }

    public ObjectProperty<InetSocketAddress> addressProperty() {
        return address;
    }

    public void setAddress(InetSocketAddress address) {
        this.address.set(address);
    }

    public ObservableList<SourcePlayerInfo> getPlayers() {
        return players.get();
    }

    public ListProperty<SourcePlayerInfo> playersProperty() {
        return players;
    }

    public void setPlayers(ObservableList<SourcePlayerInfo> players) {
        this.players.set(players);
    }

    public String getServerTags() {
        return serverTags.get();
    }

    public StringProperty serverTagsProperty() {
        return serverTags;
    }

    public void setServerTags(String serverTags) {
        this.serverTags.set(serverTags);
    }

    public String getMapName() {
        return mapName.get();
    }

    public StringProperty mapNameProperty() {
        return mapName;
    }

    public void setMapName(String mapName) {
        this.mapName.set(mapName);
    }

    public int getMaxPlayerCount() {
        return maxPlayerCount.get();
    }

    public IntegerProperty maxPlayerCountProperty() {
        return maxPlayerCount;
    }

    public void setMaxPlayerCount(int maxPlayerCount) {
        this.maxPlayerCount.set(maxPlayerCount);
    }

    public String getIpAddress() {
        return ipAddress.get();
    }

    public StringProperty ipAddressProperty() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress.set(ipAddress);
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

    public int getPlayerCount() {
        return playerCount.get();
    }

    public IntegerProperty playerCountProperty() {
        return playerCount;
    }

    public void setPlayerCount(int playerCount) {
        this.playerCount.set(playerCount);
    }

    public long getLastUpdate() {
        return lastUpdate.get();
    }

    public LongProperty lastUpdateProperty() {
        return lastUpdate;
    }

    public void setLastUpdate(long lastUpdate) {
        this.lastUpdate.set(lastUpdate);
    }
}
