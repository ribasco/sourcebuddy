package com.ibasco.sourcebuddy.service;

import com.ibasco.agql.protocols.valve.source.query.client.SourceQueryClient;
import com.ibasco.agql.protocols.valve.steam.master.client.MasterServerQueryClient;
import com.ibasco.sourcebuddy.domain.ServerDetails;
import com.ibasco.sourcebuddy.repository.CountryRepository;
import com.ibasco.sourcebuddy.repository.ServerDetailsRepository;
import com.ibasco.sourcebuddy.service.impl.SourceServerServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class SourceServerQueryServiceTest {

    private static final Logger log = LoggerFactory.getLogger(SourceServerQueryServiceTest.class);

    @Mock
    private ServerDetailsRepository sourceServerDetailsDao;

    @Mock
    private MasterServerQueryClient masterServerQueryClient;

    @Mock
    private SourceQueryClient sourceServerQueryClient;

    @Mock
    private CountryRepository countryDao;

    @Mock
    private GeoIpService geoIpService;

    @InjectMocks
    private SourceServerService sourceServerQueryService = new SourceServerServiceImpl();

    @Test
    @DisplayName("Test default populate method with an empty list")
    void populateServerListWithEmptyArgList() {
        log.info("Bean: {}", sourceServerQueryService);
        List<ServerDetails> serverList = new ArrayList<>();

        int res = sourceServerQueryService.findServerListByApp(serverList, null).join();

        log.info("Result: {}", res);
    }

    @Test
    @DisplayName("Test default populate method with non-empty arg list")
    void populateServerListWithNonEmptyArgList() {

    }
}