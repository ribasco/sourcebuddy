package com.ibasco.sourcebuddy.service;

import com.ibasco.sourcebuddy.domain.SteamApp;
import com.ibasco.sourcebuddy.tasks.UpdateServerDetailsTask;
import com.ibasco.sourcebuddy.util.SpringUtil;
import javafx.concurrent.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ServerDetailsUpdateService extends ListenableTaskService<Void> {

    private static final Logger log = LoggerFactory.getLogger(ServerDetailsUpdateService.class);

    private SteamQueryService steamQueryService;

    @Override
    protected void initialize() {
        log.debug("Initializing specific service properties for {}", getClass().getSimpleName());
        setDelay(javafx.util.Duration.seconds(5));
        setPeriod(javafx.util.Duration.seconds(300));
    }

    @Override
    protected Task<Void> createNewTask() {
        //TODO: Fixed id for now
        Optional<SteamApp> steamApp = steamQueryService.findSteamAppById(550);
        if (steamApp.isEmpty())
            throw new IllegalStateException("Steam App not found for id");
        return SpringUtil.getBean(UpdateServerDetailsTask.class, steamApp);

    }

    @Override
    protected void cancelled() {
        log.info("Service cancelled: {}", this.getClass().getSimpleName());
    }

    @Autowired
    public void setSteamQueryService(SteamQueryService steamQueryService) {
        this.steamQueryService = steamQueryService;
    }
}
