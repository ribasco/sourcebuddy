package com.ibasco.sourcebuddy.components.rcon;

import javafx.beans.property.*;

public class SourceModExtension {

    private IntegerProperty index = new SimpleIntegerProperty();

    private StringProperty name = new SimpleStringProperty();

    private StringProperty version = new SimpleStringProperty();

    private StringProperty description = new SimpleStringProperty();

    private StringProperty filename = new SimpleStringProperty();

    private StringProperty fullName = new SimpleStringProperty();

    private BooleanProperty loaded = new SimpleBooleanProperty();

    private StringProperty author = new SimpleStringProperty();

    private StringProperty binaryInfo = new SimpleStringProperty();

    private StringProperty method = new SimpleStringProperty();

    public int getIndex() {
        return index.get();
    }

    public IntegerProperty indexProperty() {
        return index;
    }

    public void setIndex(int index) {
        this.index.set(index);
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

    public String getFilename() {
        return filename.get();
    }

    public StringProperty filenameProperty() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename.set(filename);
    }

    public String getFullName() {
        return fullName.get();
    }

    public StringProperty fullNameProperty() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName.set(fullName);
    }

    public Boolean isLoaded() {
        return loaded.getValue();
    }

    public BooleanProperty loadedProperty() {
        return loaded;
    }

    public void setLoaded(Boolean loaded) {
        this.loaded.setValue(loaded);
    }

    public String getAuthor() {
        return author.get();
    }

    public StringProperty authorProperty() {
        return author;
    }

    public void setAuthor(String author) {
        this.author.set(author);
    }

    public String getBinaryInfo() {
        return binaryInfo.get();
    }

    public StringProperty binaryInfoProperty() {
        return binaryInfo;
    }

    public void setBinaryInfo(String binaryInfo) {
        this.binaryInfo.set(binaryInfo);
    }

    public String getMethod() {
        return method.get();
    }

    public StringProperty methodProperty() {
        return method;
    }

    public void setMethod(String method) {
        this.method.set(method);
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
