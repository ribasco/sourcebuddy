package com.ibasco.sourcebuddy.service;

import com.ibasco.sourcebuddy.constants.Qualifiers;
import com.ibasco.sourcebuddy.domain.ServerDetails;
import com.ibasco.sourcebuddy.domain.SteamApp;
import com.ibasco.sourcebuddy.util.WorkProgressCallback;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Service responsible for updating server related details (player, rules, server info etc)
 *
 * @author Rafael Ibasco
 */
public interface SourceServerService {

    @Async(Qualifiers.TASK_EXECUTOR_SERVICE)
    CompletableFuture<Void> updateAllServerDetails(List<ServerDetails> servers, WorkProgressCallback<ServerDetails> callback) throws InterruptedException;

    /**
     * Performs an asynchronous server info query on the list of provided servers. Note: This method will block until all queries have finished processing.
     *
     * @param servers
     *         The list of servers to query on
     * @param callback
     *         The callback for progress updates
     *
     * @throws InterruptedException
     *         thrown when the operation has been interrupted by an external process (e.g. task cancellation)
     */
    void updateServerDetails(List<ServerDetails> servers, WorkProgressCallback<ServerDetails> callback) throws InterruptedException;

    /**
     * Performs an asynchronous player query on the list of provided servers. Note: This method will block until all queries have finished processing
     *
     * @param servers
     *         The list of servers to query on
     * @param callback
     *         The callback for progress updates
     *
     * @throws InterruptedException
     *         thrown when the operation has been interrupted by an external process (e.g. task cancellation)
     */
    void updatePlayerDetails(List<ServerDetails> servers, WorkProgressCallback<ServerDetails> callback) throws InterruptedException;

    /**
     * Performs an asynchronous server rules query on the list of provided servers. Note: This method will block until all queries have finished processing
     *
     * @param servers
     *         The list of servers to query on
     * @param callback
     *         The callback for progress updates
     *
     * @throws InterruptedException
     *         thrown when the operation has been interrupted by an external process (e.g. task cancellation)
     */
    void updateServerRules(List<ServerDetails> servers, WorkProgressCallback<ServerDetails> callback) throws InterruptedException;

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
    void saveServerList(Collection<ServerDetails> servers);

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
    @Async(Qualifiers.STEAM_EXECUTOR_SERVICE)
    @Transactional(readOnly = true)
    default CompletableFuture<Integer> findServerListByApp(List<ServerDetails> servers, SteamApp app) {
        return findServerListByApp(servers, app, null);
    }

    /**
     * Retrieves a list of servers from the repository
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
    @Async(Qualifiers.STEAM_EXECUTOR_SERVICE)
    @Transactional(readOnly = true)
    CompletableFuture<Integer> findServerListByApp(List<ServerDetails> servers, SteamApp app, WorkProgressCallback<ServerDetails> callback);

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
    long fetchNewServerEntries(SteamApp app, WorkProgressCallback<ServerDetails> callback);

    boolean isBookmarked(ServerDetails server);

    void updateBookmarkFlag(ServerDetails server, boolean value);

    List<ServerDetails> findBookmarkedServers();

    int updateServerEntrieFromWebApi(SteamApp app, List<ServerDetails> servers, WorkProgressCallback<ServerDetails> callback);

    int updateServerEntriesFromMaster(SteamApp app, List<ServerDetails> servers, WorkProgressCallback<ServerDetails> callback);

    long getTotalServerEntries(SteamApp app);

}
