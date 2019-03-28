package com.ibasco.sourcebuddy.service;

import com.ibasco.sourcebuddy.domain.ServerDetails;
import com.ibasco.sourcebuddy.domain.SteamApp;
import com.ibasco.sourcebuddy.util.WorkProgressCallback;

import java.util.Collection;
import java.util.List;

public interface SourceServerService {

    void updateServerDetails(List<ServerDetails> servers, WorkProgressCallback<ServerDetails> callback) throws InterruptedException;

    void updatePlayerDetails(List<ServerDetails> servers, WorkProgressCallback<ServerDetails> callback) throws InterruptedException;

    void updateServerRules(List<ServerDetails> servers, WorkProgressCallback<ServerDetails> callback) throws InterruptedException;

    void updateCountryDetails(ServerDetails details);

    ServerDetails updateBookmarkFlag(ServerDetails server, boolean value);

    List<ServerDetails> findBookmarks(SteamApp steamApp);

    List<SteamApp> findBookmarkedSteamApps();

    default int findServerListByApp(List<ServerDetails> servers, SteamApp app) {
        return findServerListByApp(servers, app, false);
    }

    default int findServerListByApp(List<ServerDetails> servers, SteamApp app, boolean update) {
        return findServerListByApp(servers, app, update, null);
    }

    void saveServerList(Collection<ServerDetails> servers);

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
    int findServerListByApp(List<ServerDetails> servers, SteamApp app, boolean update, WorkProgressCallback<ServerDetails> callback);

    /**
     * Update and fetch new entries from the master server
     *
     * @param app
     *         The steam app
     * @param callback
     *         The progress callback. Set to null if not applicable.
     *
     * @return The total number of new server entries added to the repository
     */
    long updateServerEntries(SteamApp app, WorkProgressCallback<ServerDetails> callback);
}
