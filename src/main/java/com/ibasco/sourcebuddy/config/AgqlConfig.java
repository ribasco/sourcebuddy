package com.ibasco.sourcebuddy.config;

import com.ibasco.agql.protocols.valve.source.query.client.SourceQueryClient;
import com.ibasco.agql.protocols.valve.source.query.client.SourceRconClient;
import com.ibasco.agql.protocols.valve.source.query.logger.SourceLogListenService;
import com.ibasco.agql.protocols.valve.steam.master.client.MasterServerQueryClient;
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
public class AgqlConfig {

    private static final Logger log = LoggerFactory.getLogger(AgqlConfig.class);

    @Value("${logListenPort}")
    private int logListenPort;

    @Value("${logListenAddress}")
    private String logListenAddress;

    @Bean(name = "serverQueryClient", destroyMethod = "close")
    public SourceQueryClient serverQueryClient(@Qualifier("defaultExecutorService") ExecutorService executorService) {
        return new SourceQueryClient(executorService);
    }

    @Bean(name = "masterQueryClient", destroyMethod = "close")
    public MasterServerQueryClient masterQueryClient(@Qualifier("defaultExecutorService") ExecutorService executorService) {
        return new MasterServerQueryClient();
    }

    @Bean(name = "rconClient", destroyMethod = "close")
    public SourceRconClient rconClient(@Qualifier("defaultExecutorService") ExecutorService executorService) {
        return new SourceRconClient(true, executorService);
    }

    @Bean(name = "sourceLogListener", destroyMethod = "close")
    public SourceLogListenService sourceLogListenService(@Autowired InetAddress publicIp) {
        String address = (logListenAddress != null && !logListenAddress.isBlank()) ? logListenAddress : publicIp.getHostAddress();
        log.info("Initializing log listen service: {}:{}", address, logListenPort);
        return new SourceLogListenService(new InetSocketAddress(address, logListenPort));
    }
}
