package com.ibasco.sourcebuddy.components.rcon;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class SourceModCvarChangeResult {

    private StringProperty name = new SimpleStringProperty();

    private StringProperty value = new SimpleStringProperty();

    public SourceModCvarChangeResult() {
    }

    public SourceModCvarChangeResult(String name, String value) {
        setName(name);
        setValue(value);
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

    public String getValue() {
        return value.get();
    }

    public StringProperty valueProperty() {
        return value;
    }

    public void setValue(String value) {
        this.value.set(value);
    }

    @Override
    public String toString() {
        return "SourceModCvarChangeResult{" +
                "name='" + name + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
