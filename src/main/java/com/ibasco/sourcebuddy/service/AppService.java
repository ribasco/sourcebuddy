package com.ibasco.sourcebuddy.service;

import java.time.Duration;

public interface AppService {

    int runTaskAfter(Duration duration, Runnable action);

    boolean touchTask(Runnable action);

    boolean cancelTask(Runnable action);
}
