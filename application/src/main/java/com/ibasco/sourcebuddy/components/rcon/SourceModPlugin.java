package com.ibasco.sourcebuddy.components.rcon;

import com.ibasco.sourcebuddy.domain.ManagedServer;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class SourceModPlugin {

    private Integer index;

    private String name;

    private String filename;

    private String fullName;

    private String version;

    private String author;

    private Boolean disabled;

    private String hash;

    private String url;

    private String status;

    private LocalDateTime timestamp;

    private ManagedServer server;

    private List<SourceModCvar> sourceModCvars;

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(Boolean disabled) {
        this.disabled = disabled;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public ManagedServer getServer() {
        return server;
    }

    public void setServer(ManagedServer server) {
        this.server = server;
    }

    public List<SourceModCvar> getSourceModCvars() {
        return sourceModCvars;
    }

    public List<SourceModCvar> getCommands() {
        if (sourceModCvars == null || sourceModCvars.isEmpty())
            return null;
        return sourceModCvars.stream().filter(SourceModCvar::isCommand).collect(Collectors.toList());
    }

    public void setSourceModCvars(List<SourceModCvar> sourceModCvars) {
        this.sourceModCvars = sourceModCvars;
    }

    @Override
    public String toString() {
        return "SourceModPlugin{" +
                "index=" + index +
                ", name='" + name + '\'' +
                ", filename='" + filename + '\'' +
                ", fullName='" + fullName + '\'' +
                ", version='" + version + '\'' +
                ", author='" + author + '\'' +
                ", disabled=" + disabled +
                ", hash='" + hash + '\'' +
                ", url='" + url + '\'' +
                ", status='" + status + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
