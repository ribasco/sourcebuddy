package com.ibasco.sourcebuddy.gui;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;

import java.util.Comparator;

public class SortedListProperty<T> extends SimpleListProperty<T> {

    private ObjectProperty<Comparator<T>> comparator = new SimpleObjectProperty<>() {
        @Override
        protected void invalidated() {
            SortedList<T> sl = (SortedList<T>) SortedListProperty.this.get();
            if (sl != null)
                sl.setComparator(get());
        }
    };

    public SortedListProperty() {
    }

    public SortedListProperty(ObservableList<T> initialValue) {
        super(initialValue.sorted());
    }

    public SortedListProperty(ObservableList<T> initialValue, Comparator<T> comparator) {
        super(initialValue.sorted(comparator));
    }

    @Override
    public void set(ObservableList<T> newValue) {
        if (newValue == null) {
            super.set(null);
            return;
        }

        if (newValue instanceof SortedList)
            super.set(newValue);
        else {
            if (getComparator() == null) {
                super.set(newValue.sorted());
            } else {
                super.set(newValue.sorted(comparator.get()));
            }
        }
    }

    @Override
    protected void invalidated() {
        SortedList<T> lst = (SortedList<T>) get();
        if (lst != null) {
            lst.setComparator(getComparator());
        }
    }

    public Comparator<T> getComparator() {
        return comparator.get();
    }

    public ObjectProperty<Comparator<T>> comparatorProperty() {
        return comparator;
    }

    public void setComparator(Comparator<T> comparator) {
        this.comparator.set(comparator);
    }
}
