package com.ibasco.sourcebuddy.sourcebuddy.config;

import com.ibasco.agql.protocols.valve.source.query.client.SourceQueryClient;
import com.ibasco.agql.protocols.valve.steam.master.client.MasterServerQueryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GameQueryConfig {
    @Bean(name = "serverQueryClient", destroyMethod = "close")
    public SourceQueryClient serverQueryClient() {
        return new SourceQueryClient();
    }

    @Bean(name = "masterQueryClient", destroyMethod = "close")
    public MasterServerQueryClient masterQueryClient() {
        return new MasterServerQueryClient();
    }

}
