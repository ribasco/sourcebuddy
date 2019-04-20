package com.ibasco.sourcebuddy.gui.listeners;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.SelectionModel;

/**
 * Veto the current selection and reverts to the old selection value.
 *
 * @param <T>
 *         The type of the observable
 *
 * @see <a href="https://stackoverflow.com/a/39656197/719686">Cancelling selection change</a>
 */
abstract public class VetoChangeListener<T> implements ChangeListener<T> {

    private final SelectionModel<T> selectionModel;

    private boolean changing = false;

    public VetoChangeListener(SelectionModel<T> selectionModel) {
        if (selectionModel == null) {
            throw new IllegalArgumentException();
        }
        this.selectionModel = selectionModel;
    }

    @Override
    public void changed(ObservableValue<? extends T> observable, T oldValue, T newValue) {
        if (!changing && isInvalidChange(oldValue, newValue)) {
            changing = true;
            Platform.runLater(() -> {
                selectionModel.select(oldValue);
                changing = false;
            });
        }
    }

    protected abstract boolean isInvalidChange(T oldValue, T newValue);
}
