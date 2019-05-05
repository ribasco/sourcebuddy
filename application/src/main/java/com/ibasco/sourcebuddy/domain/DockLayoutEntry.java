package com.ibasco.sourcebuddy.domain;

import static com.ibasco.sourcebuddy.domain.DockLayoutEntry.TABLE_NAME;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.dockfx.DockPos;

import javax.persistence.*;

@Entity
@Table(name = TABLE_NAME)
public class DockLayoutEntry extends AuditableEntity<String> {

    public static final String ID = "layout_entry_id";

    public static final String POSITION = "position";

    public static final String FROM_DOCK = "from_dock_id";

    public static final String TO_DOCK = "to_dock_id";

    public static final String TABLE_NAME = "SB_DOCK_LAYOUT_ENTRY";

    private IntegerProperty id = new SimpleIntegerProperty();

    private ObjectProperty<DockLayout> layout = new SimpleObjectProperty<>();

    private ObjectProperty<DockPos> position = new SimpleObjectProperty<>();

    private ObjectProperty<DockEntry> from = new SimpleObjectProperty<>();

    private ObjectProperty<DockEntry> to = new SimpleObjectProperty<>();

    public DockLayoutEntry() {
    }

    public DockLayoutEntry(DockLayout layout, DockPos position, DockEntry from, DockEntry to) {
        setLayout(layout);
        setPosition(position);
        setFrom(from);
        setTo(to);
    }

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

    @ManyToOne
    @JoinColumn(name = DockLayout.ID, nullable = false)
    public DockLayout getLayout() {
        return layout.get();
    }

    public ObjectProperty<DockLayout> layoutProperty() {
        return layout;
    }

    public void setLayout(DockLayout layout) {
        this.layout.set(layout);
    }

    @Column(name = POSITION)
    public DockPos getPosition() {
        return position.get();
    }

    public ObjectProperty<DockPos> positionProperty() {
        return position;
    }

    public void setPosition(DockPos position) {
        this.position.set(position);
    }

    @OneToOne
    @JoinColumn(name = FROM_DOCK, nullable = false)
    public DockEntry getFrom() {
        return from.get();
    }

    public ObjectProperty<DockEntry> fromProperty() {
        return from;
    }

    public void setFrom(DockEntry from) {
        this.from.set(from);
    }

    @OneToOne
    @JoinColumn(name = TO_DOCK)
    public DockEntry getTo() {
        return to.get();
    }

    public ObjectProperty<DockEntry> toProperty() {
        return to;
    }

    public void setTo(DockEntry to) {
        this.to.set(to);
    }

    @Override
    public String toString() {
        return "DockLayoutEntry{" +
                "id=" + id.get() +
                ", layout=" + layout.get() +
                ", position=" + position.get() +
                ", from=" + from.get() +
                ", to=" + to.get() +
                '}';
    }
}
