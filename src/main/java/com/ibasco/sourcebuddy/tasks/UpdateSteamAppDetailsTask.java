package com.ibasco.sourcebuddy.tasks;

import com.ibasco.agql.core.exceptions.TooManyRequestsException;
import com.ibasco.sourcebuddy.domain.SteamApp;
import com.ibasco.sourcebuddy.domain.SteamAppDetails;
import com.ibasco.sourcebuddy.repository.SteamAppDetailsRepository;
import com.ibasco.sourcebuddy.service.SteamQueryService;
import com.ibasco.sourcebuddy.util.ThreadUtil;
import org.apache.commons.lang3.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

@Component
@Scope(scopeName = "prototype")
public class UpdateSteamAppDetailsTask extends BaseTask<Void> {

    private static final Logger log = LoggerFactory.getLogger(UpdateSteamAppDetailsTask.class);

    private SteamQueryService steamQueryService;

    private SteamAppDetailsRepository steamAppDetailsRepository;

    @Override
    protected Void process() throws Exception {

        log.debug("Updating steam app details");

        List<SteamApp> appList = steamQueryService.findSteamAppList().join();

        AtomicInteger ctr = new AtomicInteger();

        AtomicBoolean wait = new AtomicBoolean();

        AtomicInteger interval = new AtomicInteger(500);

        for (SteamApp app : appList) {
            if (steamAppDetailsRepository.findByApp(app).isPresent()) {
                log.debug("Skipping :: {}", app);
                continue;
            }
            if (wait.get()) {
                log.warn("Server throttling detected.. Increased sleep interval. Waiting for 30 seconds then restarting (New interval:: {})", interval.addAndGet(500));
                ThreadUtil.sleepUninterrupted(30000);
                wait.set(false);
            }
            steamQueryService.findAppDetails(app).whenComplete(new BiConsumer<SteamAppDetails, Throwable>() {
                @Override
                public void accept(SteamAppDetails steamAppDetails, Throwable ex) {
                    if (ex != null) {
                        if (ex instanceof CompletionException && ex.getCause() instanceof TooManyRequestsException) {
                            wait.set(true);
                            return;
                        }
                        log.error("Error find app details", ex);
                        return;
                    }

                    if (steamAppDetails != null) {
                        steamAppDetails.setSteamApp(app);
                        log.debug("Saving:: {}", steamAppDetails);
                        steamQueryService.saveSteamAppDetails(steamAppDetails);
                    }
                    //log.debug("Saved: {}", steamAppDetails);
                }
            });
            /*if (ctr.incrementAndGet() > 100) {
                break;
            }*/
            Thread.sleep(RandomUtils.nextInt(1000, 3000));
            //if (details.getType())
        }

        return null;
    }

    @Autowired
    public void setSteamAppDetailsRepository(SteamAppDetailsRepository steamAppDetailsRepository) {
        this.steamAppDetailsRepository = steamAppDetailsRepository;
    }

    @Autowired
    public void setSteamQueryService(SteamQueryService steamQueryService) {
        this.steamQueryService = steamQueryService;
    }
}
