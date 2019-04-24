package com.ibasco.sourcebuddy.service;

import com.ibasco.agql.core.utils.ServerFilter;
import com.ibasco.sourcebuddy.domain.ServerDetails;
import com.ibasco.sourcebuddy.domain.SteamApp;
import com.ibasco.sourcebuddy.domain.SteamAppDetails;
import com.ibasco.sourcebuddy.util.WorkProgressCallback;
import javafx.scene.image.Image;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@Transactional
public interface SteamService {

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
     * Retrieves a list of active game servers via the steam web service. This is equivalent to the legacy master server query.
     *
     * @param filter
     *         The filter to be applied
     * @param limit
     *         Maximum number of server entries to be retrieved
     *
     * @return The server list
     *
     * @see SourceServerService#updateServerEntriesFromMaster(SteamApp, List, WorkProgressCallback)
     */
    CompletableFuture<List<ServerDetails>> findGameServers(ServerFilter filter, int limit);

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
    default CompletableFuture<List<SteamApp>> findSteamAppList() {
        return findSteamAppList(false);
    }

    /**
     * <p>Checks the steam app repository if there are any entries available then returns the cached entries.
     * If there are no cached entries, a fresh list will be retrieved from the steam api service. This operation is executed asynchronously.</p>
     *
     * @param refresh
     *         Set to true to force fetching of new entries from the steam web service
     *
     * @return A list of {@link SteamApp} entries
     */
    CompletableFuture<List<SteamApp>> findSteamAppList(boolean refresh);

    /**
     * Retrieve a list of {@link SteamApp} with details
     *
     * @return A list of {@link SteamApp} with details
     */
    CompletableFuture<List<SteamApp>> findSteamAppsWithDetails();

    /**
     * Retrieve a list of  {@link SteamApp} from the repository
     *
     * @return A list of {@link SteamApp}
     */
    CompletableFuture<List<SteamApp>> findSteamAppsFromRepo();

    /**
     * Retrieve a list of {@link SteamApp} from the steam web service
     *
     * @return A list of {@link SteamApp}
     */
    CompletableFuture<List<SteamApp>> findSteamAppsFromWebApi();

    /**
     * Find steam app details based on the provided steam app
     *
     * @param app
     *         The {@link SteamApp} to search
     *
     * @return The details for the steam app
     */
    default CompletableFuture<SteamAppDetails> findAppDetails(SteamApp app) {
        return findAppDetails(app, false);
    }

    /**
     * Find steam app details based on the provided steam app
     *
     * @param app
     *         The {@link SteamApp} to search
     * @param processResources
     *         If true, all resources (e.g. images) will be downloaded
     *
     * @return The details for the steam app
     */
    CompletableFuture<SteamAppDetails> findAppDetails(SteamApp app, boolean processResources);

    List<SteamApp> saveSteamAppList(List<SteamApp> steamAppList);

    SteamApp saveSteamApp(SteamApp app);

    default void updateBookmarkFlag(SteamApp app) {
        app.setBookmarked(!app.isBookmarked());
        updateBookmarkFlag(app, app.isBookmarked());
    }

    void updateBookmarkFlag(SteamApp app, boolean value);

    SteamAppDetails saveSteamAppDetails(SteamAppDetails steamAppDetails);

    void saveSteamAppDetails(Iterable<SteamAppDetails> steamAppDetails);

    CompletableFuture<Image> fetchHeaderImage(SteamAppDetails details);

    CompletableFuture<byte[]> fetchHeaderImageRaw(SteamAppDetails details);
}
