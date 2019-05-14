package com.ibasco.sourcebuddy.components.rcon;

public class SourceModExtension {

    private Integer index;

    private String name;

    private String version;

    private String description;

    private String filename;

    private String fullName;

    private Boolean loaded;

    private String author;

    private String binaryInfo;

    private String method;

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

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public Boolean isLoaded() {
        return loaded;
    }

    public void setLoaded(Boolean loaded) {
        this.loaded = loaded;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getBinaryInfo() {
        return binaryInfo;
    }

    public void setBinaryInfo(String binaryInfo) {
        this.binaryInfo = binaryInfo;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    @Override
    public String toString() {
        return "SourceModExtension{" +
                "index=" + index +
                ", name='" + name + '\'' +
                ", version='" + version + '\'' +
                ", description='" + description + '\'' +
                ", filename='" + filename + '\'' +
                ", fullName='" + fullName + '\'' +
                ", loaded=" + loaded +
                ", author='" + author + '\'' +
                ", binaryInfo='" + binaryInfo + '\'' +
                ", method='" + method + '\'' +
                '}';
    }
}
