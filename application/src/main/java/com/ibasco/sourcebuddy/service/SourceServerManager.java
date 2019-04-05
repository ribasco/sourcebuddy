package com.ibasco.sourcebuddy.service;

import com.ibasco.sourcebuddy.domain.ServerDetails;

public interface SourceServerManager {

    void addManagedServer(ServerDetails server);

    void removeManagedServer(ServerDetails server);

    boolean isManaged(ServerDetails server);
}
