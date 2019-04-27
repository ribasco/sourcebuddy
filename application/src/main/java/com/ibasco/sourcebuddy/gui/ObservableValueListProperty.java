package com.ibasco.sourcebuddy.gui;

import javafx.beans.Observable;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Callback;

import java.util.List;

public class ObservableValueListProperty<T> extends SimpleListProperty<T> {

    private Callback<T, Observable[]> extractor;

    public ObservableValueListProperty(Callback<T, Observable[]> extractor) {
        this(FXCollections.observableArrayList(), extractor);
    }

    public ObservableValueListProperty(ObservableList<T> backingList, Callback<T, Observable[]> extractor) {
        super(wrapExtractor(backingList, extractor));
        this.extractor = extractor;
    }

    private static <V> ObservableList<V> wrapExtractor(List<V> lst, Callback<V, Observable[]> extractor) {
        return FXCollections.observableList(lst, extractor);
    }

    public void set(List<T> newValue) {
        if (newValue == null) {
            super.set(null);
            return;
        }
        set(FXCollections.observableArrayList(newValue));
    }

    @Override
    public void set(ObservableList<T> newValue) {
        if (newValue == null) {
            super.set(null);
            return;
        }
        if (get() != newValue) {
            super.set(wrapExtractor(newValue, extractor));
        }
    }
}
