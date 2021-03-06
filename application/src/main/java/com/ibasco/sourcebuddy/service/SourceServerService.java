package com.ibasco.sourcebuddy.service;

import com.ibasco.sourcebuddy.domain.ServerDetails;
import com.ibasco.sourcebuddy.domain.SteamApp;
import com.ibasco.sourcebuddy.util.WorkProgressCallback;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Service responsible for updating server related details (player, rules, server info etc)
 *
 * @author Rafael Ibasco
 */
public interface SourceServerService {

    /**
     * Updates server info, rules and player details
     *
     * @param server
     *         The server instance to be updated
     *
     * @return A completable future of the updated server details
     */
    CompletableFuture<ServerDetails> updateAllDetails(ServerDetails server);

    void updateAllDetails(List<ServerDetails> servers, WorkProgressCallback<ServerDetails> callback);

    /**
     * Update details of a single server instance
     *
     * @param server
     *         The server to be updated
     *
     * @return A completable future of the updated server details
     */
    CompletableFuture<ServerDetails> updateServerDetails(ServerDetails server);

    CompletableFuture<ServerDetails> updatePlayerDetails(ServerDetails server);

    CompletableFuture<ServerDetails> updateServerRules(ServerDetails server);

    /**
     * Performs an asynchronous server info query on the list of provided servers. Note: This method will block until all queries have finished processing.
     *
     * @param servers
     *         The list of servers to query on
     * @param callback
     *         The callback for progress updates
     */
    void updateServerDetails(List<ServerDetails> servers, WorkProgressCallback<ServerDetails> callback);

    /**
     * Performs an asynchronous player query on the list of provided servers. Note: This method will block until all queries have finished processing
     *
     * @param servers
     *         The list of servers to query on
     * @param callback
     *         The callback for progress updates
     */
    void updatePlayerDetails(List<ServerDetails> servers, WorkProgressCallback<ServerDetails> callback);

    /**
     * Performs an asynchronous server rules query on the list of provided servers. Note: This method will block until all queries have finished processing
     *
     * @param servers
     *         The list of servers to query on
     * @param callback
     *         The callback for progress updates
     */
    void updateServerRules(List<ServerDetails> servers, WorkProgressCallback<ServerDetails> callback);

    /**
     * Retrieves the server details of the address specified (if applicable)
     *
     * @param address
     *         The server address to query
     *
     * @return The server details or null if no server information was received from the server
     */
    CompletableFuture<ServerDetails> findServerDetails(InetSocketAddress address);

    /**
     * Updates the country information of a single server.
     *
     * @param details
     *         The server instance to be updated
     */
    void updateCountryDetails(ServerDetails details);

    /**
     * Retrieve a list of bookmarked steam apps
     *
     * @return A list of {@link SteamApp}
     */
    List<SteamApp> findBookmarkedSteamApps();

    /**
     * Save a list of servers
     *
     * @param servers
     *         List of servers to save
     */
    void save(Collection<ServerDetails> servers);

    /**
     * Save a single server entity to the database
     *
     * @param server
     *         The server to be saved
     */
    void save(ServerDetails server);

    boolean exists(ServerDetails serverDetails);

    boolean exists(InetSocketAddress address);

    /**
     * Retrieves a list of servers from the repository
     *
     * @param servers
     *         The server list
     * @param app
     *         The steam app to query
     *
     * @return The number of servers fetch from the repository
     */
    default int findServerListByApp(Collection<ServerDetails> servers, SteamApp app) {
        return findServerListByApp(servers, app, null);
    }

    /**
     * Retrieves a list of servers from the repository. If the provided server list argument is not empty, new entries will be merged into it. Existing entries will be replaced .
     *
     * @param servers
     *         The server list
     * @param app
     *         The steam app to query
     * @param callback
     *         Callback progress
     *
     * @return The number of new servers added to the list
     */
    int findServerListByApp(Collection<ServerDetails> servers, SteamApp app, WorkProgressCallback<ServerDetails> callback);

    /**
     * Fetch new server entries from the master server
     *
     * @param app
     *         The steam app to use as our search criteria
     *
     * @return The total number of new server entries added to the repository
     */
    default long fetchNewServerEntries(SteamApp app) {
        return fetchNewServerEntries(app, null);
    }

    /**
     * Fetch new server entries from the master server
     *
     * @param app
     *         The steam app to use as our search criteria
     * @param callback
     *         The progress callback. Set to null if not applicable.
     *
     * @return The total number of new server entries added to the repository
     */
    int fetchNewServerEntries(SteamApp app, WorkProgressCallback<ServerDetails> callback);

    boolean isBookmarked(ServerDetails server);

    void updateBookmarkFlag(ServerDetails server, boolean value);

    List<ServerDetails> findBookmarkedServers();

    List<ServerDetails> findBookmarkedServers(SteamApp app);

    int updateServerEntrieFromWebApi(SteamApp app, Collection<ServerDetails> servers, WorkProgressCallback<ServerDetails> callback);

    int updateServerEntriesFromMaster(SteamApp app, Collection<ServerDetails> servers, WorkProgressCallback<ServerDetails> callback);

    long getTotalServerEntries(SteamApp app);

}
