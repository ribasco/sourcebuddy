package com.ibasco.sourcebuddy.components;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class TaskManager {

    private static final Logger log = LoggerFactory.getLogger(TaskManager.class);

    @PostConstruct
    private void init() {
        log.debug("TaskManager :: initialize()");
    }
}
