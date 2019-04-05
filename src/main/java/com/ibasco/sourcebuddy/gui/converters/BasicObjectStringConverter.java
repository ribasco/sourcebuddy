package com.ibasco.sourcebuddy.gui.converters;

import javafx.util.StringConverter;

public class BasicObjectStringConverter<T> extends StringConverter<T> {

    @Override
    public String toString(T object) {
        return object != null ? object.toString() : "";
    }

    @Override
    public T fromString(String string) {
        return null;
    }
}
