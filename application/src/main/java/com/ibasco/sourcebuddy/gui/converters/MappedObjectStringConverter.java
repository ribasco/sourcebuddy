package com.ibasco.sourcebuddy.gui.converters;

import com.ibasco.sourcebuddy.util.Check;
import javafx.util.StringConverter;

import java.util.function.Function;

public class MappedObjectStringConverter<T> extends StringConverter<T> {

    private Function<T, String> mapper;

    public MappedObjectStringConverter(Function<T, String> mapper) {
        this.mapper = Check.requireNonNull(mapper, "Mapper cannot be null");
    }

    @Override
    public String toString(T object) {
        return object != null ? mapper.apply(object) : "";
    }

    @Override
    public T fromString(String string) {
        return null;
    }
}
