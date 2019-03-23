package com.ibasco.sourcebuddy.config;

import com.ibasco.sourcebuddy.service.*;
import com.ibasco.sourcebuddy.service.impl.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServiceConfig {

    @Bean
    public SourceServerQueryService sourceServerQueryService() {
        return new SourceServerQueryServiceImpl();
    }

    @Bean
    public SteamQueryService steamQueryService() {
        return new SteamQueryServiceImpl();
    }

    @Bean
    public GeoIpService geoIpService() {
        return new GeoIpServiceImpl();
    }

    @Bean
    public MasterServerUpdateService masterServerUpdateService() {
        return new MasterServerUpdateService();
    }

    @Bean
    public ServerPurgeService serverPurgeService() {
        return new ServerPurgeServiceImpl();
    }

    @Bean
    public NetworkDiagnosticService networkDiagnosticService() {
        return new NetworkDiagnosticServiceImpl();
    }

    @Bean
    public MapLookupService mapLookupService() {
        return new SourceMapLookupServiceImpl();
    }
}
