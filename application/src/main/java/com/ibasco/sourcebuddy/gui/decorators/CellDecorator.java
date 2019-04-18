package com.ibasco.sourcebuddy.gui.decorators;

import javafx.scene.control.IndexedCell;

import java.util.Objects;

@FunctionalInterface
public interface CellDecorator<S, T> {

    void decorate(T item, IndexedCell<S> cell);

    default CellDecorator<S, T> andThen(CellDecorator<S, T> after) {
        Objects.requireNonNull(after);
        return (l, r) -> {
            decorate(l, r);
            after.decorate(l, r);
        };
    }
}
