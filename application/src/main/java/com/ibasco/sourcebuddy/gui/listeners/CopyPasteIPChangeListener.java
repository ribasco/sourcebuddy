package com.ibasco.sourcebuddy.gui.listeners;

import com.ibasco.sourcebuddy.util.Check;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class CopyPasteIPChangeListener implements ChangeListener<String> {

    private final Consumer<String[]> action;

    private AtomicBoolean flag = new AtomicBoolean();

    public CopyPasteIPChangeListener(Consumer<String[]> action) {
        this.action = Check.requireNonNull(action, "Action cannot be null");
    }

    @Override
    public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
        if (!flag.get() && !StringUtils.isBlank(newValue)) {
            if (StringUtils.containsAny(newValue, " ", "-", ";", ":", ",")) {
                String[] values = newValue.trim().split("[\\s:,;\\-]");
                if (values.length == 2) {
                    try {
                        flag.set(true);
                        if (action != null)
                            action.accept(values);
                    } finally {
                        flag.set(false);
                    }
                }
            }
        }
    }
}
