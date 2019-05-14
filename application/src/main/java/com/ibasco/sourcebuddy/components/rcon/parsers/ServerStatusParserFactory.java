package com.ibasco.sourcebuddy.components.rcon.parsers;

import com.ibasco.sourcebuddy.components.rcon.SourceServerStatus;
import com.ibasco.sourcebuddy.components.rcon.parsers.status.DefaultSourceServerStatusParser;
import com.ibasco.sourcebuddy.domain.ManagedServer;
import org.springframework.stereotype.Component;

@Component
public class ServerStatusParserFactory {

    public RconResultParser<SourceServerStatus> create(ManagedServer server) {
        if (server.getServerDetails() == null) {
            throw new IllegalStateException("Managed server instance does have a valid server detail information");
        }
        return new DefaultSourceServerStatusParser();
    }
}
