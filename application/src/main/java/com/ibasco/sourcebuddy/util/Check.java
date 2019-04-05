package com.ibasco.sourcebuddy.util;

public class Check {

    public static <T> T requireNonNull(T object, String message) {
        if (object == null)
            throw new IllegalArgumentException(message);
        return object;
    }
}
