package com.ibasco.sourcebuddy.util;

import com.ibasco.sourcebuddy.domain.ServerDetails;
import com.ibasco.sourcebuddy.enums.ServerStatus;

public class ServerDetailsFilter {

    public static boolean byNonEmptyServers(ServerDetails server) {
        return server.getPlayerCount() > 0;
    }

    public static boolean byActiveServers(ServerDetails server) {
        return server.getStatus() == ServerStatus.ACTIVE;
    }

}
