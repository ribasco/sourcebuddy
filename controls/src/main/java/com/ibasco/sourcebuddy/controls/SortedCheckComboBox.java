package com.ibasco.sourcebuddy.controls;

import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import org.controlsfx.control.CheckComboBox;

import java.util.Comparator;

public class SortedCheckComboBox<T> extends CheckComboBox<T> {

    private ObjectProperty<Comparator<T>> comparator = new SimpleObjectProperty<>() {
        @Override
        protected void invalidated() {
            if (SortedCheckComboBox.this.getItems() != null) {
                SortedList<T> slist = (SortedList<T>) SortedCheckComboBox.this.getItems();
                slist.setComparator(get());
            }
        }
    };

    private ListProperty<T> backingList = new SimpleListProperty<>(FXCollections.observableArrayList());

    public SortedCheckComboBox() {
        this(FXCollections.observableArrayList(), null);
    }

    public SortedCheckComboBox(ObservableList<T> items, Comparator<T> comparator) {
        super(wrapSortedList(items, comparator));
        this.comparator.set(comparator);
        this.backingList.set(items);
    }

    private static <X> SortedList<X> wrapSortedList(ObservableList<X> lst, Comparator<X> comparator) {
        if (lst instanceof SortedList) {
            return (SortedList<X>) lst;
        }
        if (comparator == null)
            return lst.sorted();
        return lst.sorted(comparator);
    }

    public ObservableList<T> getBackingList() {
        return backingList.get();
    }

    public ListProperty<T> backingListProperty() {
        return backingList;
    }

    public void setBackingList(ObservableList<T> backingList) {
        this.backingList.set(backingList);
    }

    public Comparator getComparator() {
        return comparator.get();
    }

    public ObjectProperty<Comparator<T>> comparatorProperty() {
        return comparator;
    }

    public void setComparator(Comparator<T> comparator) {
        this.comparator.set(comparator);
    }
}
