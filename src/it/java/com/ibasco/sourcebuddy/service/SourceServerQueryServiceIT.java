package com.ibasco.sourcebuddy.service;

import com.ibasco.sourcebuddy.domain.ServerDetails;
import com.ibasco.sourcebuddy.domain.SteamApp;
import com.ibasco.sourcebuddy.util.WorkProgressCallback;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@SpringBootTest
class SourceServerQueryServiceIT {

    private static final Logger log = LoggerFactory.getLogger(SourceServerQueryServiceTest.class);

    @Autowired
    private SourceServerService sourceServerQueryService;

    @Autowired
    private SteamService steamQueryService;

    @Test
    @DisplayName("Test default populate method with an empty list")
    void test01() {

        Optional<SteamApp> steamApp = steamQueryService.findSteamAppById(550);

        log.debug("Got steam app: {}", steamApp);

        assertTrue(steamApp.isPresent());

        List<ServerDetails> serverList = new ArrayList<>();

        int res = sourceServerQueryService.findServerListByApp(serverList, steamApp.get(), new WorkProgressCallback<ServerDetails>() {
            @Override
            public void onProgress(ServerDetails item, Throwable ex) {
                log.debug("Server: {}", item);
            }
        }).join();

        assertTrue(res > 0);
    }

    @Test
    @DisplayName("Test default populate method with non-empty arg list")
    void test02() {

    }
}