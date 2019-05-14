package com.ibasco.sourcebuddy.service;

import com.ibasco.sourcebuddy.domain.ManagedServer;
import com.ibasco.sourcebuddy.domain.ServerDetails;
import com.ibasco.sourcebuddy.domain.SteamApp;

import java.util.List;

public interface ServerManager {

    ManagedServer addServer(ServerDetails server);

    void removeServer(ServerDetails server);

    void removeServer(ManagedServer server);

    boolean isManaged(ServerDetails server);

    ManagedServer save(ManagedServer server);

    void deleteAllServers();

    List<ManagedServer> findManagedServer();

    List<ManagedServer> findManagedServer(SteamApp app);

    ManagedServer findManagedServer(ServerDetails server);
}
