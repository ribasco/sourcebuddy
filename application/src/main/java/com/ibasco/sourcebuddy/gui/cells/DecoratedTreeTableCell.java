package com.ibasco.sourcebuddy.gui.cells;

import com.ibasco.sourcebuddy.gui.decorators.CellDecorator;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.TreeTableCell;

public class DecoratedTreeTableCell<S, T> extends TreeTableCell<S, T> {

    private CellDecorator<S, T> decorator;

    public DecoratedTreeTableCell(CellDecorator<S, T> decorator) {
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
            if (decorator != null) {
                //noinspection unchecked
                decorator.decorate(item, (IndexedCell<S>) this);
            } else {
                setText(item.toString());
            }
        }
    }

    public CellDecorator<S, T> getDecorator() {
        return decorator;
    }
}
