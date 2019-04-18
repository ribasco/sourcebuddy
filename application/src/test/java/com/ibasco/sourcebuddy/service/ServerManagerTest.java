package com.ibasco.sourcebuddy.service;

import com.ibasco.sourcebuddy.domain.ManagedServer;
import com.ibasco.sourcebuddy.domain.ServerDetails;
import com.ibasco.sourcebuddy.domain.SteamApp;
import com.ibasco.sourcebuddy.model.ServerDetailsModel;
import com.ibasco.sourcebuddy.repository.ManagedServerRepository;
import org.apache.commons.lang3.RandomUtils;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ServerManagerTest {

    private static final Logger log = LoggerFactory.getLogger(ServerManagerTest.class);

    @Autowired
    private SourceServerService sourceServerService;

    @Autowired
    private ServerManager serverManager;

    @Autowired
    private SteamService steamService;

    private List<ServerDetails> serverDetailsList = new ArrayList<>();

    @Autowired
    private ConfigService configService;

    @Autowired
    private ManagedServerRepository managedServerRepository;

    @Autowired
    private ServerDetailsModel serverDetailsModel;

    private ServerDetails serverDetails;

    private SteamApp app;

    @BeforeAll
    void beforeAll() {
        log.info("Executing before all");
        Optional<SteamApp> res = steamService.findSteamAppById(550);
        if (res.isPresent()) {
            app = res.get();
            sourceServerService.findServerListByApp(serverDetailsList, app).join();
            assertTrue(serverDetailsList.size() > 0);
        }
        serverDetails = serverDetailsList.get(RandomUtils.nextInt(0, serverDetailsList.size()));
        log.debug("Selected server details: {}", serverDetails);
    }

    @Test
    @DisplayName("Add server")
    void test01() {
        int beforeSize = serverDetailsModel.getActiveProfile().getManagedServers().size();
        log.debug("Adding server details: {}", serverDetails);
        ManagedServer managedServer = serverManager.addServer(serverDetails);

        assertEquals(beforeSize + 1, serverDetailsModel.getActiveProfile().getManagedServers().size());
        assertNotNull(managedServer);
        assertNotNull(managedServer.getServerDetails());
        assertNotNull(managedServer.getProfile());

        serverDetailsModel.getActiveProfile().getManagedServers().forEach(s -> log.debug("\t- Managed server: {}", s));
        assertTrue(serverDetailsModel.getActiveProfile().getManagedServers().contains(managedServer));
    }

    @Test
    @DisplayName("Delete server")
    void test02() {
        /*ManagedServer managedServer = serverManager.findManagedServer(serverDetailsList.get(0));
        assertNotNull(managedServer);*/
        List<ManagedServer> servers = serverManager.findManagedServer();
        if (!servers.isEmpty()) {
            ManagedServer managedServer = servers.get(0);
            serverManager.removeServer(managedServer);
            assertFalse(serverManager.isManaged(managedServer.getServerDetails()));
            assertFalse(serverDetailsModel.getServerDetails().contains(managedServer.getServerDetails()));
        }
    }
/*
    @Test
    @DisplayName("Find managed server")
    void test03() {
        Optional<SteamApp> res = steamService.findSteamAppById(550);
        if (res.isPresent()) {
            ServerDetails sd = serverDetailsList.get(0);
            assertFalse(serverManager.isManaged(sd));
            ManagedServer managedServer = serverManager.addServer(sd);
            List<ManagedServer> managedServers = serverManager.findManagedServer(managedServer.getServerDetails().getSteamApp());
            assertNotNull(managedServers);
            assertTrue(managedServers.size() > 0);
        }
    }*/
}