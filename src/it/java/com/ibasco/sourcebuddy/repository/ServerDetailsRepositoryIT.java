package com.ibasco.sourcebuddy.repository;

import com.ibasco.sourcebuddy.domain.ServerDetails;
import com.ibasco.sourcebuddy.domain.SteamApp;
import com.ibasco.sourcebuddy.service.SteamQueryService;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;

@SpringBootTest
public class ServerDetailsRepositoryIT {

    private static final Logger log = LoggerFactory.getLogger(ServerDetailsRepositoryIT.class);

    @Autowired
    private ServerDetailsRepository serverDetailsRepository;

    @Autowired
    private SteamQueryService steamQueryService;

    @Test
    @DisplayName("Test findSteamAppById()")
    public void test01() {
        Optional<SteamApp> steamApp = steamQueryService.findSteamAppById(550);

        assertTrue(steamApp.isPresent());

        List<ServerDetails> detailsList = serverDetailsRepository.findBySteamApp(steamApp.get());

        assertTrue(detailsList.size() > 0);
    }
}
