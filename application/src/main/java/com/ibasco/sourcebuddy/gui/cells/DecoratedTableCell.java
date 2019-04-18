package com.ibasco.sourcebuddy.gui.cells;

import com.ibasco.sourcebuddy.gui.decorators.CellDecorator;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.TableCell;

public class DecoratedTableCell<S, T> extends TableCell<S, T> {

    private CellDecorator<S, T> decorator;

    public DecoratedTableCell(CellDecorator<S, T> decorator) {
        super();
        this.decorator = decorator;
    }

    @Override
    protected void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);
        if (item == null || empty) {
            setText(null);
            setGraphic(null);
            setStyle(null);
        } else {
            //noinspection unchecked
            decorator.decorate(item, (IndexedCell<S>) this);
        }
    }
}
