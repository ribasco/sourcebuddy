package com.ibasco.sourcebuddy.config;

import com.ibasco.sourcebuddy.service.GeoIpService;
import com.ibasco.sourcebuddy.service.MapLookupService;
import com.ibasco.sourcebuddy.service.ServerPurgeService;
import com.ibasco.sourcebuddy.service.SteamService;
import com.ibasco.sourcebuddy.service.impl.GeoIpServiceImpl;
import com.ibasco.sourcebuddy.service.impl.ServerPurgeServiceImpl;
import com.ibasco.sourcebuddy.service.impl.SourceMapLookupServiceImpl;
import com.ibasco.sourcebuddy.service.impl.SteamServiceImpl;
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
    public MapLookupService mapLookupService() {
        return new SourceMapLookupServiceImpl();
    }
}
