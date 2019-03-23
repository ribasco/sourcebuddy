package com.ibasco.sourcebuddy.service;

import com.ibasco.sourcebuddy.domain.SteamApp;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

@SpringBootTest
public class SteamQueryServiceIT {

    private static final Logger log = LoggerFactory.getLogger(SteamQueryServiceIT.class);

    @Autowired
    private SteamQueryService steamQueryService;

    @Test
    @DisplayName("Test steam app refresh")
    public void tsst01() {
        int total = steamQueryService.updateSteamAppsRepository().join();
        log.debug("Refreshed a total of {} entries", total);
    }

    @Test
    public void test02() {
        Optional<SteamApp> app = steamQueryService.findSteamAppById(550);
        assertNotNull(app);
        log.debug("App: {}", app.get().getName());
    }
}
