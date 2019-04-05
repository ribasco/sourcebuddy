package com.ibasco.sourcebuddy.controls;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.cell.ComboBoxListCell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A custom {@link ComboBox} with a progress indicator
 *
 * @param <T>
 *         The type of the value that has been selected or otherwise entered
 */
public class ProgressComboBox<T> extends ComboBox<T> {

    private static final Logger log = LoggerFactory.getLogger(ProgressComboBox.class);

    private ProgressIndicator progressIndicator;

    private ComboBoxListCell<T> buttonCell = new ComboBoxListCell<>() {
        @Override
        public void updateItem(T item, boolean empty) {
            super.updateItem(item, empty);
            if (item == null || empty) {
                setText(null);
                setGraphic(null);
            } else {
                setText(item.toString());
                if (isShowProgress()) {
                    setGraphic(progressIndicator);
                } else {
                    setGraphic(null);
                }
            }
        }
    };

    private DoubleProperty progress = new SimpleDoubleProperty() {
        @Override
        protected void invalidated() {
            progressIndicator.setProgress(get());
        }
    };

    private BooleanProperty showProgress = new SimpleBooleanProperty() {
        @Override
        protected void invalidated() {
            progressIndicator.setVisible(get());
            setDisable(get());
            if (get()) {
                setButtonCell(buttonCell);
            } else
                setButtonCell(null);
        }
    };

    public ProgressComboBox() {
        this(FXCollections.observableArrayList());
    }

    public ProgressComboBox(ObservableList<T> items) {
        super(items);
        progressIndicator = new ProgressIndicator();
        progressIndicator.setPrefSize(16, 16);
        progressIndicator.setProgress(-1);
        getStyleClass().add("progress-combo-box");
    }

    @Override
    public ObjectProperty<ListCell<T>> buttonCellProperty() {
        return super.buttonCellProperty();
    }

    public double getProgress() {
        return progress.get();
    }

    public DoubleProperty progressProperty() {
        return progress;
    }

    public void setProgress(double progress) {
        this.progress.set(progress);
    }

    public boolean isShowProgress() {
        return showProgress.get();
    }

    public BooleanProperty showProgressProperty() {
        return showProgress;
    }

    public void setShowProgress(boolean showProgress) {
        this.showProgress.set(showProgress);
    }
}
