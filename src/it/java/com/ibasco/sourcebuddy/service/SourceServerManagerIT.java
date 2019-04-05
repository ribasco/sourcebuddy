package com.ibasco.sourcebuddy.service;

import com.ibasco.agql.core.utils.ServerFilter;
import com.ibasco.sourcebuddy.Bootstrap;
import com.ibasco.sourcebuddy.domain.ServerDetails;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

@SpringBootTest(classes = Bootstrap.class)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SourceServerManagerIT {

    private static final Logger log = LoggerFactory.getLogger(SourceServerManagerIT.class);

    @Autowired
    private SourceServerManager sourceServerManager;

    @Autowired
    private SourceServerService sourceServerService;

    @Autowired
    private SteamService steamService;

    private List<ServerDetails> serverList;

    @BeforeAll
    private void setup() {
        log.debug("Initializing app cache");
        serverList = steamService.findGameServers(ServerFilter.create().dedicated(true).appId(550), 10).join();
        assertEquals(10, serverList.size());
    }

    @Test
    void testAddServer() {
        log.info("Add managed server test");



        /*ServerDetails details = new ServerDetails();
        sourceServerManager.addManagedServer(details);
        assertTrue(sourceServerManager.isManaged(details));*/
    }
}