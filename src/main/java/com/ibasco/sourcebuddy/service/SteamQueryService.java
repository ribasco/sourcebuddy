package com.ibasco.sourcebuddy.service;

import com.ibasco.agql.core.utils.ServerFilter;
import com.ibasco.sourcebuddy.domain.ServerDetails;
import com.ibasco.sourcebuddy.domain.SteamApp;
import com.ibasco.sourcebuddy.domain.SteamAppDetails;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public interface SteamQueryService {

    /**
     * Update the steam app cache from the repository
     *
     * @return The number of new app entries added to the reposit
     */
    CompletableFuture<Integer> updateSteamAppsRepository();

    /**
     * Find a steam app by id
     *
     * @param id
     *         The steam app id
     *
     * @return The {@link SteamApp} associated with the provided id
     */
    Optional<SteamApp> findSteamAppById(int id);

    /**
     * Retrieve game servers from the master via web api
     *
     * @param filter
     *         The search criteria
     * @param limit
     *         Number of records to fetch
     *
     * @return The list of game servers
     */
    CompletableFuture<Void> findGameServers(ServerFilter filter, int limit, Consumer<ServerDetails> callback);

    /**
     * <p>Checks the steam app repository if there are any entries available then returns the cached entries.
     * If there are no cached entries, a fresh list will be retrieved from the steam api service. This operation is executed asynchronously.</p>
     *
     * @return A list of {@link SteamApp} entries
     */
    CompletableFuture<List<SteamApp>> findSteamAppList();

    List<SteamApp> findSteamAppsFromRepo();

    List<SteamApp> findSteamAppsFromWebApi();

    void updateSteamAppCache();

    void updateSteamAppCache(List<SteamApp> steamAppList);

    /**
     * Find steam app details based on the provided steam app
     *
     * @param app
     *         The {@link SteamApp} to search
     *
     * @return The details for the steam app
     */
    CompletableFuture<SteamAppDetails> findAppDetails(SteamApp app);

    void saveSteamAppList(List<SteamApp> steamAppList);
}
