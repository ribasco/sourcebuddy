package com.ibasco.sourcebuddy.gui.tableview.cells;

import com.ibasco.sourcebuddy.controllers.FragmentController;
import javafx.scene.Node;
import javafx.scene.control.TableCell;

abstract public class ViewFragmentCell<S, T, C extends FragmentController> extends TableCell<S, T> {

    private C controller;

    protected abstract void processItem(T item);

    @Override
    protected void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);
        if (item == null || empty) {
            setText(null);
            setStyle("");
            setGraphic(null);
        } else {
            processItem(item);
        }
    }

    private void setController(C controller) {
        this.controller = controller;
    }

    protected C getController() {
        return controller;
    }

    protected <X extends Node> X getRootNode() {
        //noinspection unchecked
        return (X) controller.getRootNode();
    }
}
