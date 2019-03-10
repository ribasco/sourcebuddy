package com.ibasco.sourcebuddy.sourcebuddy.config;

import com.ibasco.agql.protocols.valve.source.query.client.SourceQueryClient;
import com.ibasco.agql.protocols.valve.source.query.client.SourceRconClient;
import com.ibasco.agql.protocols.valve.source.query.logger.SourceLogListenService;
import com.ibasco.agql.protocols.valve.steam.master.client.MasterServerQueryClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.net.InetAddress;
import java.net.InetSocketAddress;

@Configuration
@PropertySource("classpath:source.properties")
public class GameQueryConfig {

    private static final Logger log = LoggerFactory.getLogger(GameQueryConfig.class);

    @Value("${logListenPort}")
    private int logListenPort;

    @Value("${logListenAddress}")
    private String logListenAddress;

    @Bean(name = "serverQueryClient", destroyMethod = "close")
    public SourceQueryClient serverQueryClient() {
        return new SourceQueryClient();
    }

    @Bean(name = "masterQueryClient", destroyMethod = "close")
    public MasterServerQueryClient masterQueryClient() {
        return new MasterServerQueryClient();
    }

    @Bean(name = "rconClient", destroyMethod = "close")
    public SourceRconClient rconClient() {
        return new SourceRconClient();
    }

    @Bean(name = "sourceLogListener", destroyMethod = "close")
    public SourceLogListenService sourceLogListenService(@Autowired InetAddress publicIp) {
        SourceLogListenService service = new SourceLogListenService();
        String address = (logListenAddress != null && !logListenAddress.isBlank()) ? logListenAddress : publicIp.getHostAddress();
        log.info("Initializing log listen service: {}:{}", address, logListenPort);
        service.setListenAddress(new InetSocketAddress(address, logListenPort));
        return service;
    }
}
