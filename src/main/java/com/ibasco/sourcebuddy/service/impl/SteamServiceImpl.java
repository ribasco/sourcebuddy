package com.ibasco.sourcebuddy.service.impl;

import com.ibasco.agql.core.utils.ServerFilter;
import com.ibasco.agql.protocols.valve.steam.webapi.interfaces.SteamApps;
import com.ibasco.agql.protocols.valve.steam.webapi.interfaces.SteamGameServerService;
import com.ibasco.agql.protocols.valve.steam.webapi.interfaces.SteamStorefront;
import com.ibasco.sourcebuddy.components.EntityMapper;
import com.ibasco.sourcebuddy.constants.Qualifiers;
import com.ibasco.sourcebuddy.domain.ServerDetails;
import com.ibasco.sourcebuddy.domain.SteamApp;
import com.ibasco.sourcebuddy.domain.SteamAppDetails;
import com.ibasco.sourcebuddy.repository.SteamAppDetailsRepository;
import com.ibasco.sourcebuddy.repository.SteamAppsRepository;
import com.ibasco.sourcebuddy.service.SteamService;
import javafx.scene.image.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class SteamServiceImpl implements SteamService {

    private static final Logger log = LoggerFactory.getLogger(SteamServiceImpl.class);

    private SteamAppsRepository steamAppRepository;

    private SteamAppDetailsRepository steamAppDetailsRepository;

    private SteamApps steamAppsApi;

    private SteamStorefront steamStorefrontApi;

    private SteamGameServerService steamGameServerService;

    private EntityMapper entityMapper;

    private HttpClient httpClient;

    @Override
    @Async(Qualifiers.STEAM_EXECUTOR_SERVICE)
    public CompletableFuture<Integer> updateSteamAppsRepository() {
        log.debug("Refreshing steam apps cache in repository");
        List<SteamApp> appList = steamAppsApi.getAppList().thenApply(entityMapper::mapSteamAppList).join();
        if (appList.size() > 0) {
            log.debug("Saving {} new entries to the repository", appList.size());
            steamAppRepository.saveAll(appList);
        }
        return CompletableFuture.completedFuture(appList.size());
    }

    @Override
    public Optional<SteamApp> findSteamAppById(int id) {
        return steamAppRepository.findById(id);
    }

    @Override
    public CompletableFuture<List<ServerDetails>> findGameServers(ServerFilter filter, int limit) {
        return steamGameServerService.getServerList(filter, limit).thenApply(steamSourceServers -> {
            log.debug("findGameServers() :: Got total of {} entries from the web service", steamSourceServers.size());
            return steamSourceServers.parallelStream().map(entityMapper::map).collect(Collectors.toList());
        });
    }

    @Override
    public CompletableFuture<Void> findGameServers(ServerFilter filter, int limit, Consumer<ServerDetails> callback) {
        return steamGameServerService.getServerList(filter, limit).thenAccept(steamSourceServers -> {
            log.debug("findGameServers() :: Got total of {} entries from the web service", steamSourceServers.size());
            steamSourceServers.parallelStream().map(entityMapper::map).forEach(callback);
        });
    }

    @Override
    public CompletableFuture<List<SteamApp>> findSteamAppList(boolean refresh) {
        log.debug("findSteamAppList() :: Fetching from Steam App Repository");

        final AtomicInteger repoAppListSize = new AtomicInteger();

        return findSteamAppsFromRepo()
                .thenCompose(steamAppList -> {
                    repoAppListSize.set(steamAppList.size());
                    if (!refresh && repoAppListSize.get() > 0) {
                        log.debug("Returning results from steam app repository");
                        return CompletableFuture.completedFuture(steamAppList);
                    }
                    return findSteamAppsFromWebApi();
                }).thenApply(steamAppList -> {
                    //Only save if there is a difference in size
                    if (steamAppList.size() != repoAppListSize.get()) {
                        log.debug("Saving steam app list to repository");
                        saveSteamAppList(steamAppList);
                        log.debug("Saved {} entries to app repository", steamAppList.size());
                    }
                    return steamAppList;
                });
    }

    @Override
    @Async(Qualifiers.STEAM_EXECUTOR_SERVICE)
    public CompletableFuture<List<SteamApp>> findSteamAppsWithDetails() {
        return CompletableFuture.completedFuture(steamAppRepository.findSteamAppListNonEmpty(Sort.by(Sort.Direction.DESC, "bookmarked").and(Sort.by(Sort.Direction.ASC, "id"))));
    }

    @Override
    @Async(Qualifiers.STEAM_EXECUTOR_SERVICE)
    public CompletableFuture<List<SteamApp>> findSteamAppsFromRepo() {
        return CompletableFuture.completedFuture(steamAppRepository.findSteamAppListNonEmpty(Sort.by(Sort.Direction.DESC, "bookmarked").and(Sort.by(Sort.Direction.ASC, "id"))));
    }

    @Override
    public CompletableFuture<List<SteamApp>> findSteamAppsFromWebApi() {
        return steamAppsApi.getAppList().thenApply(entityMapper::mapSteamAppList);
    }

    @Override
    public CompletableFuture<SteamAppDetails> findAppDetails(SteamApp app, boolean processResources) {
        Optional<SteamAppDetails> details = steamAppDetailsRepository.findByApp(app);
        if (details.isPresent()) {
            //log.debug("Retrieved from repository :: {}", details.get());
            return CompletableFuture.completedFuture(details.get());
        }
        return steamStorefrontApi.getAppDetails(app.getId())
                .thenApply(e -> entityMapper.map(e, app))
                .thenApply(appDetails -> {
                    if (appDetails == null) {
                        //if no details are present, create a new entry
                        appDetails = new SteamAppDetails();
                        appDetails.setName(app.getName());
                        appDetails.setEmptyDetails(true);
                    }
                    //Set steam app
                    appDetails.setSteamApp(app);
                    return appDetails;
                })
                .thenApply(appDetails -> {
                    if (!processResources)
                        return appDetails;
                    //Fetch image
                    if (!appDetails.isEmptyDetails()) {
                        appDetails.setHeaderImage(fetchHeaderImageRaw(appDetails).join());
                    }
                    return appDetails;
                })
                .thenApply(appDetails -> {
                    log.debug("Saving app details to repository: {}, App: {})", appDetails, appDetails.getSteamApp());
                    if (steamAppDetailsRepository.findByApp(app).isEmpty()) {
                        saveSteamAppDetails(appDetails);
                    }
                    return appDetails;
                });
    }

    @Override
    public List<SteamApp> saveSteamAppList(List<SteamApp> steamAppList) {
        return steamAppRepository.saveAll(steamAppList);
    }

    @Override
    public SteamApp saveSteamApp(SteamApp app) {
        return steamAppRepository.saveAndFlush(app);
    }

    @Override
    public void updateBookmarkFlag(SteamApp app, boolean value) {
        app.setBookmarked(value);
        steamAppRepository.updateBookmark(app, value);
    }

    @Override
    public SteamAppDetails saveSteamAppDetails(SteamAppDetails steamAppDetails) {
        return steamAppDetailsRepository.save(steamAppDetails);
    }

    @Override
    public CompletableFuture<Image> fetchHeaderImage(SteamAppDetails details) {
        HttpRequest request = HttpRequest.newBuilder(URI.create(details.getHeaderImageUrl())).GET().header("Content-Type", "image/jpeg").build();
        CompletableFuture<HttpResponse<byte[]>> res = httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofByteArray());
        return res.thenApply(httpResponse -> new Image(new ByteArrayInputStream(httpResponse.body())));
    }

    @Override
    public CompletableFuture<byte[]> fetchHeaderImageRaw(SteamAppDetails details) {
        HttpRequest request = HttpRequest.newBuilder(URI.create(details.getHeaderImageUrl())).GET().header("Content-Type", "image/jpeg").build();
        CompletableFuture<HttpResponse<byte[]>> res = httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofByteArray());
        return res.thenApply(HttpResponse::body);
    }

    @Autowired
    public void setSteamAppRepository(SteamAppsRepository steamAppRepository) {
        this.steamAppRepository = steamAppRepository;
    }

    @Autowired
    public void setSteamAppsApi(SteamApps steamAppsApi) {
        this.steamAppsApi = steamAppsApi;
    }

    @Autowired
    public void setSteamStorefrontApi(SteamStorefront steamStorefrontApi) {
        this.steamStorefrontApi = steamStorefrontApi;
    }

    @Autowired
    public void setSteamGameServerService(SteamGameServerService steamGameServerService) {
        this.steamGameServerService = steamGameServerService;
    }

    @Autowired
    public void setEntityMapper(EntityMapper entityMapper) {
        this.entityMapper = entityMapper;
    }

    @Autowired
    public void setSteamAppDetailsRepository(SteamAppDetailsRepository steamAppDetailsRepository) {
        this.steamAppDetailsRepository = steamAppDetailsRepository;
    }

    @Autowired
    public void setHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }
}
