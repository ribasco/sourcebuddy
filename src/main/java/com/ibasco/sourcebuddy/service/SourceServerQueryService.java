package com.ibasco.sourcebuddy.service;

import com.ibasco.sourcebuddy.domain.ServerDetails;
import com.ibasco.sourcebuddy.domain.SteamApp;
import com.ibasco.sourcebuddy.util.WorkProgressCallback;

import java.util.List;

public interface SourceServerQueryService {

    default void updateServerDetails(List<ServerDetails> servers) throws InterruptedException {
        updateServerDetails(servers, null);
    }

    void updateServerDetails(List<ServerDetails> servers, WorkProgressCallback<ServerDetails> callback) throws InterruptedException;

    default void updatePlayerDetails(List<ServerDetails> servers) throws InterruptedException {
        updatePlayerDetails(servers, null);
    }

    void updatePlayerDetails(List<ServerDetails> servers, WorkProgressCallback<ServerDetails> callback) throws InterruptedException;

    default void updateServerRules(List<ServerDetails> servers) throws InterruptedException {
        updatePlayerDetails(servers, null);
    }

    void updateServerRules(List<ServerDetails> servers, WorkProgressCallback<ServerDetails> callback) throws InterruptedException;

    void updateCountryDetails(ServerDetails details);

    default int populateServerList(List<ServerDetails> servers, SteamApp app) {
        return populateServerList(servers, app, false);
    }

    default int populateServerList(List<ServerDetails> servers, SteamApp app, boolean update) {
        return populateServerList(servers, app, update, null);
    }

    /**
     * Populates the list with source server details
     *
     * @param servers
     *         The server list
     * @param app
     *         The steam app to query
     * @param update
     *         If true, the server list will sync with the steam master servers
     * @param callback
     *         Callback progress
     *
     * @return The number of new servers added to the list
     */
    int populateServerList(List<ServerDetails> servers, SteamApp app, boolean update, WorkProgressCallback<ServerDetails> callback);

    /**
     * Populate the current server repository with new server IP addresses obtained from the Master Server
     */
    void updateMasterServerList();
}
