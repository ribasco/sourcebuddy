package com.ibasco.sourcebuddy.components.rcon;

import com.ibasco.sourcebuddy.domain.ManagedServer;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;

import java.util.Map;

public class SourceServerStatus {

    private StringProperty name = new SimpleStringProperty();

    private ObjectProperty<SourceServerVersion> version = new SimpleObjectProperty<>();

    private StringProperty localIp = new SimpleStringProperty();

    private StringProperty publicIp = new SimpleStringProperty();

    private IntegerProperty localPort = new SimpleIntegerProperty();

    private IntegerProperty publicPort = new SimpleIntegerProperty();

    private StringProperty operatingSystem = new SimpleStringProperty();

    private StringProperty type = new SimpleStringProperty();

    private StringProperty map = new SimpleStringProperty();

    private IntegerProperty humanCount = new SimpleIntegerProperty();

    private IntegerProperty botCount = new SimpleIntegerProperty();

    private BooleanProperty hibernating = new SimpleBooleanProperty();

    private IntegerProperty maxPlayerCount = new SimpleIntegerProperty();

    private StringProperty steamId = new SimpleStringProperty();

    private LongProperty steamId64 = new SimpleLongProperty();

    private BooleanProperty secured = new SimpleBooleanProperty();

    private ListProperty<String> tags = new SimpleListProperty<>();

    private ListProperty<SourcePlayerStatus> players = new SimpleListProperty<>();

    private MapProperty<String, String> entries = new SimpleMapProperty<>();

    private ObjectProperty<ManagedServer> server = new SimpleObjectProperty<>();

    private ListProperty<String> headers = new SimpleListProperty<>();

    private BooleanProperty reserved = new SimpleBooleanProperty();

    private StringProperty reservationCookie = new SimpleStringProperty();

    private IntegerProperty edicts = new SimpleIntegerProperty();

    private IntegerProperty maxEdicts = new SimpleIntegerProperty();

    public static class SourceServerVersion {

        private String version;

        private String type;

        private String meta;

        public SourceServerVersion() {
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getMeta() {
            return meta;
        }

        public void setMeta(String meta) {
            this.meta = meta;
        }

        @Override
        public String toString() {
            return "Version{" +
                    "version='" + version + '\'' +
                    ", type='" + type + '\'' +
                    ", meta='" + meta + '\'' +
                    '}';
        }
    }

    public SourceServerStatus(ManagedServer server) {
        this(server, null);
    }

    public SourceServerStatus(ManagedServer server, Map<String, String> statusEntries) {
        setServer(server);
        setEntries(statusEntries != null ? FXCollections.observableMap(statusEntries) : FXCollections.observableHashMap());
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

    public SourceServerVersion getVersion() {
        return version.get();
    }

    public ObjectProperty<SourceServerVersion> versionProperty() {
        return version;
    }

    public void setVersion(SourceServerVersion version) {
        this.version.set(version);
    }

    public String getLocalIp() {
        return localIp.get();
    }

    public StringProperty localIpProperty() {
        return localIp;
    }

    public void setLocalIp(String localIp) {
        this.localIp.set(localIp);
    }

    public String getPublicIp() {
        return publicIp.get();
    }

    public StringProperty publicIpProperty() {
        return publicIp;
    }

    public void setPublicIp(String publicIp) {
        this.publicIp.set(publicIp);
    }

    public Integer getLocalPort() {
        return localPort.getValue();
    }

    public IntegerProperty localPortProperty() {
        return localPort;
    }

    public void setLocalPort(Integer localPort) {
        this.localPort.setValue(localPort);
    }

    public Integer getPublicPort() {
        return publicPort.getValue();
    }

    public IntegerProperty publicPortProperty() {
        return publicPort;
    }

    public void setPublicPort(Integer publicPort) {
        this.publicPort.setValue(publicPort);
    }

    public String getOperatingSystem() {
        return operatingSystem.get();
    }

    public StringProperty operatingSystemProperty() {
        return operatingSystem;
    }

    public void setOperatingSystem(String operatingSystem) {
        this.operatingSystem.set(operatingSystem);
    }

    public String getType() {
        return type.get();
    }

    public StringProperty typeProperty() {
        return type;
    }

    public void setType(String type) {
        this.type.set(type);
    }

    public String getMap() {
        return map.get();
    }

    public StringProperty mapProperty() {
        return map;
    }

    public void setMap(String map) {
        this.map.set(map);
    }

    public int getHumanCount() {
        return humanCount.get();
    }

    public IntegerProperty humanCountProperty() {
        return humanCount;
    }

    public void setHumanCount(int humanCount) {
        this.humanCount.set(humanCount);
    }

    public int getBotCount() {
        return botCount.get();
    }

    public IntegerProperty botCountProperty() {
        return botCount;
    }

    public void setBotCount(int botCount) {
        this.botCount.set(botCount);
    }

    public boolean isHibernating() {
        return hibernating.get();
    }

    public BooleanProperty hibernatingProperty() {
        return hibernating;
    }

    public void setHibernating(boolean hibernating) {
        this.hibernating.set(hibernating);
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

    public String getSteamId() {
        return steamId.get();
    }

    public StringProperty steamIdProperty() {
        return steamId;
    }

    public void setSteamId(String steamId) {
        this.steamId.set(steamId);
    }

    public long getSteamId64() {
        return steamId64.get();
    }

    public LongProperty steamId64Property() {
        return steamId64;
    }

    public void setSteamId64(long steamId64) {
        this.steamId64.set(steamId64);
    }

    public boolean isSecured() {
        return secured.get();
    }

    public BooleanProperty securedProperty() {
        return secured;
    }

    public void setSecured(boolean secured) {
        this.secured.set(secured);
    }

    public ObservableList<String> getTags() {
        return tags.get();
    }

    public ListProperty<String> tagsProperty() {
        return tags;
    }

    public void setTags(ObservableList<String> tags) {
        this.tags.set(tags);
    }

    public ObservableList<SourcePlayerStatus> getPlayers() {
        return players.get();
    }

    public ListProperty<SourcePlayerStatus> playersProperty() {
        return players;
    }

    public void setPlayers(ObservableList<SourcePlayerStatus> players) {
        this.players.set(players);
    }

    public ObservableMap<String, String> getEntries() {
        return entries.get();
    }

    public MapProperty<String, String> entriesProperty() {
        return entries;
    }

    public void setEntries(ObservableMap<String, String> entries) {
        this.entries.set(entries);
    }

    public ManagedServer getServer() {
        return server.get();
    }

    public ObjectProperty<ManagedServer> serverProperty() {
        return server;
    }

    public void setServer(ManagedServer server) {
        this.server.set(server);
    }

    public ObservableList<String> getHeaders() {
        return headers.get();
    }

    public ListProperty<String> headersProperty() {
        return headers;
    }

    public void setHeaders(ObservableList<String> headers) {
        this.headers.set(headers);
    }

    public boolean isReserved() {
        return reserved.get();
    }

    public BooleanProperty reservedProperty() {
        return reserved;
    }

    public void setReserved(boolean reserved) {
        this.reserved.set(reserved);
    }

    public String getReservationCookie() {
        return reservationCookie.get();
    }

    public StringProperty reservationCookieProperty() {
        return reservationCookie;
    }

    public void setReservationCookie(String reservationCookie) {
        this.reservationCookie.set(reservationCookie);
    }

    public int getEdicts() {
        return edicts.get();
    }

    public IntegerProperty edictsProperty() {
        return edicts;
    }

    public void setEdicts(int edicts) {
        this.edicts.set(edicts);
    }

    public int getMaxEdicts() {
        return maxEdicts.get();
    }

    public IntegerProperty maxEdictsProperty() {
        return maxEdicts;
    }

    public void setMaxEdicts(int maxEdicts) {
        this.maxEdicts.set(maxEdicts);
    }
}
