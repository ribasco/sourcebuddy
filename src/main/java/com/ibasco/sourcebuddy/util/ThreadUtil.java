package com.ibasco.sourcebuddy.util;

public class ThreadUtil {
    public static void sleepUninterrupted(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignored) {
        }
    }
}
