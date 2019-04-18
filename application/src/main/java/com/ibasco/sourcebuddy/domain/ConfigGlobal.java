package com.ibasco.sourcebuddy.domain;

import static com.ibasco.sourcebuddy.domain.ConfigGlobal.TABLE_NAME;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = TABLE_NAME)
public class ConfigGlobal extends AuditableEntity<String> {

    public static final String TABLE_NAME = "SB_CONFIG_GLOBAL";

    public static final String KEY = "key";

    public static final String VALUE = "value";

    private StringProperty key = new SimpleStringProperty();

    private StringProperty value = new SimpleStringProperty();

    public ConfigGlobal() {
    }

    public ConfigGlobal(String key, Object value) {
        setKey(key);
        setValue(value != null ? value.toString() : null);
    }

    @Id
    @Column(name = KEY, unique = true)
    public String getKey() {
        return key.get();
    }

    public StringProperty keyProperty() {
        return key;
    }

    public void setKey(String key) {
        this.key.set(key);
    }

    @Column(name = VALUE)
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
