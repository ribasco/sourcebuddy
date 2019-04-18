package com.ibasco.sourcebuddy.service;

import com.ibasco.sourcebuddy.domain.ConfigProfile;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ConfigServiceTest {

    private static final Logger log = LoggerFactory.getLogger(ConfigServiceTest.class);

    @Autowired
    private ConfigService configService;

    @Test
    @DisplayName("Create and save profile")
    void testCreate() {
        log.debug("service: {}", configService);
        ConfigProfile profile = configService.createProfile();
        profile.setName("TestProfile");
        configService.saveProfile(profile);
    }

    @Test
    void testGetDefaultProfile() {
        ConfigProfile profile = configService.getDefaultProfile();
        assertNotNull(profile);
    }

    @Test
    void testSaveGlobalKey() {
        configService.saveGlobalConfig("TEST_KEY", 100);
        String value = configService.getGlobalConfig("TEST_KEY");

        assertEquals(100, value);
    }
}