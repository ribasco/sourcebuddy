package com.ibasco.sourcebuddy.config;

import com.ibasco.agql.protocols.valve.source.query.client.SourceQueryClient;
import com.ibasco.agql.protocols.valve.source.query.client.SourceRconClient;
import com.ibasco.agql.protocols.valve.source.query.logger.SourceLogListenService;
import com.ibasco.agql.protocols.valve.steam.master.client.MasterServerQueryClient;
import com.ibasco.agql.protocols.valve.steam.webapi.SteamWebApiClient;
import com.ibasco.agql.protocols.valve.steam.webapi.interfaces.SteamApps;
import com.ibasco.agql.protocols.valve.steam.webapi.interfaces.SteamStorefront;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;

@Configuration
@PropertySource("classpath:source.properties")
public class SteamConfig {

    private static final Logger log = LoggerFactory.getLogger(SteamConfig.class);

    @Value("${logListenPort}")
    private int logListenPort;

    @Value("${logListenAddress}")
    private String logListenAddress;

    @Value("${app.steam-auth-token}")
    private String authToken;

    @Bean(destroyMethod = "close")
    public SourceQueryClient sourceServerQueryClient(@Qualifier("defaultExecutorService") ExecutorService executorService) {
        return new SourceQueryClient(executorService);
    }

    @Bean(destroyMethod = "close")
    public MasterServerQueryClient masterServerQueryClient(@Qualifier("defaultExecutorService") ExecutorService executorService) {
        return new MasterServerQueryClient();
    }

    @Bean(destroyMethod = "close")
    public SourceRconClient sourceRconClient(@Qualifier("defaultExecutorService") ExecutorService executorService) {
        return new SourceRconClient(true, executorService);
    }

    @Bean(destroyMethod = "close")
    public SourceLogListenService sourceLogListener(@Autowired InetAddress publicIp) {
        String address = (logListenAddress != null && !logListenAddress.isBlank()) ? logListenAddress : publicIp.getHostAddress();
        log.info("Initializing log listen service: {}:{}", address, logListenPort);
        return new SourceLogListenService(new InetSocketAddress(address, logListenPort));
    }

    @Bean
    public SteamApps steamAppsApi() {
        return new SteamApps(steamWebApiClient());
    }

    @Bean
    public SteamWebApiClient steamWebApiClient() {
        return new SteamWebApiClient(authToken);
    }

    @Bean
    public SteamStorefront steamStorefrontApi() {
        return new SteamStorefront(steamWebApiClient());
    }
}
