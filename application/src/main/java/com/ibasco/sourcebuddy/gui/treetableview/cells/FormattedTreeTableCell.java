package com.ibasco.sourcebuddy.gui.treetableview.cells;

import javafx.scene.control.TreeTableCell;

import java.util.Collections;
import java.util.function.Predicate;

public class FormattedTreeTableCell<S, T> extends TreeTableCell<S, T> {

    private String styleClass;

    private Predicate<S> predicate;

    public FormattedTreeTableCell(String styleClass, Predicate<S> predicate) {
        this.styleClass = styleClass;
        this.predicate = predicate;
    }

    @Override
    protected void updateItem(T item, boolean empty) {
        if (item == null || empty) {
            setText(null);
            setGraphic(null);
        } else {
            setText(item.toString());
            S rowItem = getTreeTableRow().getItem();
            if (rowItem == null)
                return;
            if (predicate.test(rowItem)) {
                if (!getStyleClass().contains(styleClass))
                    getStyleClass().add(styleClass);
            } else {
                getStyleClass().removeAll(Collections.singleton(styleClass));
            }
        }
        super.updateItem(item, empty);
    }
}
