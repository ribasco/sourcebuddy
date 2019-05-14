package com.ibasco.sourcebuddy.domain;

import static com.ibasco.sourcebuddy.domain.DockLayout.TABLE_NAME;
import javafx.beans.property.*;

import javax.persistence.*;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = TABLE_NAME,
        uniqueConstraints = @UniqueConstraint(name = "UNQ_NAME_PROFILE", columnNames = {DockLayout.NAME, ConfigProfile.ID})
)
public class DockLayout extends AuditableEntity<String> {

    public static final String ID = "layout_id";

    public static final String NAME = "name";

    public static final String LOCKED = "locked";

    public static final String TABLE_NAME = "SB_DOCK_LAYOUT";

    private IntegerProperty id = new SimpleIntegerProperty();

    private StringProperty name = new SimpleStringProperty();

    private Set<DockLayoutEntry> layoutEntries = new LinkedHashSet<>();

    private ObjectProperty<ConfigProfile> profile = new SimpleObjectProperty<>();

    private BooleanProperty locked = new SimpleBooleanProperty(false);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = ID)
    public int getId() {
        return id.get();
    }

    public IntegerProperty idProperty() {
        return id;
    }

    public void setId(int id) {
        this.id.set(id);
    }

    @Column(name = NAME, nullable = false)
    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    @OneToMany(mappedBy = "layout", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    public Set<DockLayoutEntry> getLayoutEntries() {
        return layoutEntries;
    }

    public void setLayoutEntries(Set<DockLayoutEntry> layoutEntries) {
        this.layoutEntries = layoutEntries;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = ConfigProfile.ID)
    public ConfigProfile getProfile() {
        return profile.get();
    }

    public ObjectProperty<ConfigProfile> profileProperty() {
        return profile;
    }

    public void setProfile(ConfigProfile profile) {
        this.profile.set(profile);
    }

    @Column(name = LOCKED)
    public Boolean isLocked() {
        return locked.getValue();
    }

    public BooleanProperty lockedProperty() {
        return locked;
    }

    public void setLocked(Boolean locked) {
        this.locked.setValue(locked);
    }

    @Override
    public String toString() {
        return String.format("%s (Profile: %s)", getName(), getProfile());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DockLayout layout = (DockLayout) o;
        return getId() == layout.getId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
