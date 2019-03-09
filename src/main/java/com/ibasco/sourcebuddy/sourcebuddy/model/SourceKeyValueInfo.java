package com.ibasco.sourcebuddy.sourcebuddy.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class SourceKeyValueInfo {
    private StringProperty key = new SimpleStringProperty();

    private StringProperty value = new SimpleStringProperty();

    public SourceKeyValueInfo(String key, Object value) {
        setKey(key);
        if (value == null || "".equals(value.toString().trim()))
            setValue("N/A");
        else
            setValue(String.valueOf(value));
    }

    public String getKey() {
        return key.get();
    }

    public StringProperty keyProperty() {
        return key;
    }

    public void setKey(String key) {
        this.key.set(key);
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
}
