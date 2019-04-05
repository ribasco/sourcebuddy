package com.ibasco.sourcebuddy.domain;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class KeyValueInfo {

    private StringProperty key = new SimpleStringProperty();

    private StringProperty value = new SimpleStringProperty();

    public KeyValueInfo(String key, Object value) {
        setKey(key);
        if (value == null || "".equals(value.toString().trim()))
            setValue("N/A");
        else
            setValue(String.valueOf(value));
    }

    public String getKey() {
        return key.get();
    }

    public void setKey(String key) {
        this.key.set(key);
    }

    public StringProperty keyProperty() {
        return key;
    }

    public String getValue() {
        return value.get();
    }

    public void setValue(String value) {
        this.value.set(value);
    }

    public StringProperty valueProperty() {
        return value;
    }
}
