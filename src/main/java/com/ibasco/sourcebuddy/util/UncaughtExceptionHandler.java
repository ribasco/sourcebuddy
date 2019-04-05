package com.ibasco.sourcebuddy.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(UncaughtExceptionHandler.class);

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        log.error("Uncaught exception occured", e);
    }
}
