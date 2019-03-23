package com.ibasco.sourcebuddy.service;

import com.ibasco.sourcebuddy.domain.SteamApp;
import com.ibasco.sourcebuddy.domain.SteamAppDetails;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

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

    CompletableFuture<Integer> updateSteamAppDetailsRepository();

    /**
     * <p>Checks the steam app repository if there are any entries available then returns the cached entries.
     * If there are no cached entries, a fresh list will be retrieved from the steam api service. This operation is executed asynchronously.</p>
     *
     * @return A list of {@link SteamApp} entries
     */
    CompletableFuture<List<SteamApp>> findSteamApps();

    CompletableFuture<SteamAppDetails> findAppDetails(SteamApp app);
}
