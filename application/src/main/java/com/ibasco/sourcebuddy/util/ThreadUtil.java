package com.ibasco.sourcebuddy.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThreadUtil {

    private static final Logger log = LoggerFactory.getLogger(ThreadUtil.class);

    public static void sleepUninterrupted(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignored) {
            log.debug("Thread interrupted");
        }
    }
}
