package com.ibasco.sourcebuddy.util;

public class Check {

    public static <T> T requireNonNull(T object, String message) {
        if (object == null)
            throw new IllegalArgumentException(message);
        return object;
    }

    public static String requireNonBlank(String str, String message) {
        if (str == null || "".equals(str.trim()))
            throw new IllegalArgumentException(message);
        return str;
    }
}
