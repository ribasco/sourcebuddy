package com.ibasco.sourcebuddy.service;

import java.time.Duration;

public interface AppService {

    int runAfter(Duration duration, Runnable action);

    boolean reset(Runnable action);

    boolean cancelTask(Runnable action);
}
