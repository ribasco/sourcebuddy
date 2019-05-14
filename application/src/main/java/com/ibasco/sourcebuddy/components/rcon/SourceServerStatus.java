package com.ibasco.sourcebuddy.components.rcon;

import com.ibasco.sourcebuddy.domain.ManagedServer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SourceServerStatus {

    private String name;

    private Version version;

    private String localIp;

    private String publicIp;

    private Integer localPort;

    private Integer publicPort;

    private String operatingSystem;

    private String type;

    private String map;

    private Integer humanCount;

    private Integer botCount;

    private Boolean hibernating;

    private Integer maxPlayerCount;

    private String steamId;

    private Long steamId64;

    private Boolean secured;

    private List<String> tags;

    private List<SourcePlayerStatus> players;

    private Map<String, String> entries;

    private ManagedServer server;

    private List<String> headers;

    private Boolean reserved;

    private String reservationCookie;

    private Integer edicts;

    private Integer maxEdicts;

    public static class Version {

        private String version;

        private String type;

        private String meta;

        public Version() {
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
        this.server = server;
        this.entries = statusEntries != null ? new HashMap<>(statusEntries) : new HashMap<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Version getVersion() {
        return version;
    }

    public void setVersion(Version version) {
        this.version = version;
    }

    public String getLocalIp() {
        return localIp;
    }

    public void setLocalIp(String localIp) {
        this.localIp = localIp;
    }

    public Integer getLocalPort() {
        return localPort;
    }

    public void setLocalPort(Integer localPort) {
        this.localPort = localPort;
    }

    public String getPublicIp() {
        return publicIp;
    }

    public void setPublicIp(String publicIp) {
        this.publicIp = publicIp;
    }

    public Integer getPublicPort() {
        return publicPort;
    }

    public void setPublicPort(Integer publicPort) {
        this.publicPort = publicPort;
    }

    public String getOperatingSystem() {
        return operatingSystem;
    }

    public void setOperatingSystem(String operatingSystem) {
        this.operatingSystem = operatingSystem;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMap() {
        return map;
    }

    public void setMap(String map) {
        this.map = map;
    }

    public Integer getHumanCount() {
        return humanCount;
    }

    public void setHumanCount(Integer humanCount) {
        this.humanCount = humanCount;
    }

    public Integer getBotCount() {
        return botCount;
    }

    public void setBotCount(Integer botCount) {
        this.botCount = botCount;
    }

    public boolean supportsHibernating() {
        return getHibernating() != null;
    }

    public Boolean getHibernating() {
        return hibernating;
    }

    public void setHibernating(Boolean hibernating) {
        this.hibernating = hibernating;
    }

    public Integer getMaxPlayerCount() {
        return maxPlayerCount;
    }

    public void setMaxPlayerCount(Integer maxPlayerCount) {
        this.maxPlayerCount = maxPlayerCount;
    }

    public String getSteamId() {
        return steamId;
    }

    public void setSteamId(String steamId) {
        this.steamId = steamId;
    }

    public Long getSteamId64() {
        return steamId64;
    }

    public void setSteamId64(Long steamId64) {
        this.steamId64 = steamId64;
    }

    public Boolean isSecured() {
        return secured;
    }

    public void setSecured(Boolean secured) {
        this.secured = secured;
    }

    public Map<String, String> getEntries() {
        return entries;
    }

    public void setEntries(Map<String, String> entries) {
        this.entries = entries;
    }

    public String getProperty(String key) {
        return entries.get(key);
    }

    public List<SourcePlayerStatus> getPlayers() {
        return players;
    }

    public void setPlayers(List<SourcePlayerStatus> players) {
        this.players = players;
    }

    public ManagedServer getServer() {
        return server;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public List<String> getHeaders() {
        return headers;
    }

    public void setHeaders(List<String> headers) {
        this.headers = headers;
    }

    public Boolean isReserved() {
        return reserved;
    }

    public void setReserved(Boolean reserved) {
        this.reserved = reserved;
    }

    public String getReservationCookie() {
        return reservationCookie;
    }

    public void setReservationCookie(String reservationCookie) {
        this.reservationCookie = reservationCookie;
    }

    public Integer getEdicts() {
        return edicts;
    }

    public void setEdicts(Integer edicts) {
        this.edicts = edicts;
    }

    public Integer getMaxEdicts() {
        return maxEdicts;
    }

    public void setMaxEdicts(Integer maxEdicts) {
        this.maxEdicts = maxEdicts;
    }
}
