package com.ibasco.sourcebuddy.gui.tableview.factory;

import javafx.animation.FadeTransition;
import javafx.event.EventHandler;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;

public class PulsingTableRowFactory<T> implements Callback<TableView<T>, TableRow<T>> {

    private FadeTransition pulseTransition = new FadeTransition();

    public PulsingTableRowFactory() {
        pulseTransition.setFromValue(1.0);
        pulseTransition.setToValue(0.3);
        pulseTransition.setAutoReverse(true);
        pulseTransition.setCycleCount(2);
        pulseTransition.setDuration(javafx.util.Duration.millis(200));
    }

    @Override
    public TableRow<T> call(TableView<T> param) {
        TableRow<T> row = new TableRow<>();
        row.hoverProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                pulseTransition.setNode(row);
                pulseTransition.play();
            } else {
                pulseTransition.stop();
                pulseTransition.getNode().setOpacity(1.0);
                pulseTransition.setNode(null);
            }
        });
        row.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {

            }
        });
        return row;
    }
}
