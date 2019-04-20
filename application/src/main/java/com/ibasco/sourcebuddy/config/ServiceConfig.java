package com.ibasco.sourcebuddy.config;

import com.ibasco.sourcebuddy.service.*;
import com.ibasco.sourcebuddy.service.impl.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServiceConfig {

    /*@Bean
    public SourceServerService sourceServerService() {
        return new SourceServerServiceImpl();
    }*/

    @Bean
    public SteamService steamService() {
        return new SteamServiceImpl();
    }

    @Bean
    public GeoIpService geoIpService() {
        return new GeoIpServiceImpl();
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
