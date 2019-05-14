package com.ibasco.sourcebuddy.gui.listeners;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.SplitPane;

public class ToggableSplitPaneChangeListener implements ChangeListener<Boolean> {

    private SplitPane pane;

    private Node toggableNode;

    public ToggableSplitPaneChangeListener(SplitPane pane, Node toggableNode) {
        this.pane = pane;
        this.toggableNode = toggableNode;
    }

    @Override
    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        if (newValue == null)
            return;
        if (newValue) {
            if (!pane.getItems().contains(toggableNode))
                pane.getItems().add(toggableNode);
        } else {
            pane.getItems().remove(toggableNode);
        }
    }
}
