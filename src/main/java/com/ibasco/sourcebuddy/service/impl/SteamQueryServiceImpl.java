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
import com.ibasco.sourcebuddy.repository.SteamAppsRepository;
import com.ibasco.sourcebuddy.service.SteamQueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@Service
@Transactional
public class SteamQueryServiceImpl implements SteamQueryService {

    private static final Logger log = LoggerFactory.getLogger(SteamQueryServiceImpl.class);

    private SteamAppsRepository steamAppRepository;

    private SteamApps steamAppsApi;

    private SteamStorefront steamStorefrontApi;

    private SteamGameServerService steamGameServerService;

    private EntityMapper entityMapper;

    private Map<Integer, SteamApp> steamAppCache = new HashMap<>();

    @Override
    @Async(Qualifiers.STEAM_EXECUTOR_SERVICE)
    public CompletableFuture<Integer> updateSteamAppsRepository() {
        log.debug("Refreshing steam apps cache in repository");
        List<SteamApp> appList = steamAppsApi.getAppList().thenApply(entityMapper::convert).join();
        if (appList.size() > 0) {
            log.debug("Saving {} new entries to the repository", appList.size());
            steamAppRepository.saveAll(appList);
        }
        return CompletableFuture.completedFuture(appList.size());
    }

    @Override
    public Optional<SteamApp> findSteamAppById(int id) {
        if (steamAppCache.isEmpty()) {
            log.debug("findSteamAppById() :: Cache is empty. Refreshing app cache");
            updateSteamAppCache();
        }
        if (steamAppCache.containsKey(id))
            return Optional.of(steamAppCache.get(id));
        return steamAppRepository.findById(id);
    }

    @Override
    public CompletableFuture<Void> findGameServers(ServerFilter filter, int limit, Consumer<ServerDetails> callback) {
        return steamGameServerService.getServerList(filter, limit).thenAccept(steamSourceServers -> {
            log.debug("findGameServers() :: Got total of {} entries from the web service", steamSourceServers.size());
            steamSourceServers.parallelStream().map(entityMapper::convert).forEach(callback);
        });
    }

    @Override
    @Async(Qualifiers.STEAM_EXECUTOR_SERVICE)
    public CompletableFuture<List<SteamApp>> findSteamAppList() {
        try {
            log.debug("findSteamAppList() :: Fetching from Steam App Repository");
            List<SteamApp> steamAppsList = findSteamAppsFromRepo();

            if (!steamAppsList.isEmpty())
                return CompletableFuture.completedFuture(steamAppsList);

            log.debug("fetchSteamAppList() :: Fetching a fresh list from the api service");
            return CompletableFuture.completedFuture(findSteamAppsFromWebApi());
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    public List<SteamApp> findSteamAppsFromRepo() {
        List<SteamApp> steamAppsList = steamAppRepository.findAll();
        log.debug("findSteamAppsFromRepo() :: Found {} apps from the repository", steamAppsList.size());
        if (steamAppsList.size() > 0) {
            log.debug("findSteamAppsFromRepo() :: Updating steam app cache from repository (Total: {})", steamAppsList.size());
            updateSteamAppCache(steamAppsList);
        }
        return steamAppsList;
    }

    @Override
    public List<SteamApp> findSteamAppsFromWebApi() {
        List<SteamApp> appList = steamAppsApi.getAppList().thenApply(entityMapper::convert).join();
        updateSteamAppCache(appList);
        log.debug("findSteamAppsFromWebApi() :: Saving {} entries to the repository", appList.size());
        return steamAppRepository.saveAll(appList);
    }

    @Override
    public void updateSteamAppCache() {
        log.debug("updateSteamAppCache() :: Fetching app entries from repository");
        updateSteamAppCache(steamAppRepository.findAll());
    }

    @Override
    public void updateSteamAppCache(List<SteamApp> steamAppList) {
        log.debug("updateSteamAppCache() :: Updating steam app cache with {} entries", steamAppList.size());
        steamAppList.forEach(a -> steamAppCache.put(a.getId(), a));
    }

    @Override
    public CompletableFuture<SteamAppDetails> findAppDetails(SteamApp app) {
        return steamStorefrontApi.getAppDetails(app.getId()).thenApply(entityMapper::convert);
    }

    @Override
    public void saveSteamAppList(List<SteamApp> steamAppList) {
        steamAppRepository.saveAll(steamAppList);
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
}
