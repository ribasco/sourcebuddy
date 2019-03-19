package com.ibasco.sourcebuddy.util;

import com.ibasco.sourcebuddy.entities.SourceServerDetails;
import com.ibasco.sourcebuddy.enums.ServerStatus;

public class ServerDetailsFilter {
    public static boolean byNonEmptyServers(SourceServerDetails server) {
        return server.getPlayerCount() > 0;
    }

    public static boolean byActiveServers(SourceServerDetails server) {
        return server.getStatus() == ServerStatus.ACTIVE;
    }

}
