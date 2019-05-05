package com.ibasco.sourcebuddy.domain;

import static com.ibasco.sourcebuddy.domain.DockEntry.TABLE_NAME;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.jetbrains.annotations.NotNull;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = TABLE_NAME)
public class DockEntry extends AuditableEntity<String> implements Comparable<DockEntry> {

    public static final String ID = "dock_id";

    public static final String NAME = "name";

    public static final String TABLE_NAME = "SB_DOCK_ENTRY";

    private StringProperty id = new SimpleStringProperty();

    private StringProperty name = new SimpleStringProperty();

    @Id
    @Column(name = ID, unique = true)
    public String getId() {
        return id.get();
    }

    public StringProperty idProperty() {
        return id;
    }

    public void setId(String id) {
        this.id.set(id);
    }

    @Column(name = NAME)
    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    @Override
    public int compareTo(@NotNull DockEntry o) {
        return this.getName().compareTo(o.getName());
    }

    @Override
    public String toString() {
        return name.get();
    }
}
