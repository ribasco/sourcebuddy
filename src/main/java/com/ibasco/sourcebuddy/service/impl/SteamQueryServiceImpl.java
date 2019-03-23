package com.ibasco.sourcebuddy.service.impl;

import com.ibasco.agql.protocols.valve.steam.webapi.interfaces.SteamApps;
import com.ibasco.agql.protocols.valve.steam.webapi.interfaces.SteamStorefront;
import com.ibasco.sourcebuddy.domain.SteamApp;
import com.ibasco.sourcebuddy.domain.SteamAppDetails;
import com.ibasco.sourcebuddy.repository.SteamAppsRepository;
import com.ibasco.sourcebuddy.service.SteamQueryService;
import com.ibasco.sourcebuddy.util.AgqlEntityConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
@Transactional
public class SteamQueryServiceImpl implements SteamQueryService {

    private static final Logger log = LoggerFactory.getLogger(SteamQueryServiceImpl.class);

    private SteamAppsRepository steamAppRepository;

    private SteamApps steamAppsApi;

    private SteamStorefront steamStorefrontApi;

    private AgqlEntityConverter entityConverter = new AgqlEntityConverter();

    @Async("defaultExecutorService")
    @Override
    public CompletableFuture<Integer> updateSteamAppsRepository() {
        log.debug("Refreshing steam apps cache in repository");
        List<SteamApp> appList = steamAppsApi.getAppList().thenApply(entityConverter::convert).join();
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
    public CompletableFuture<Integer> updateSteamAppDetailsRepository() {
        return null;
    }

    @Override
    public CompletableFuture<List<SteamApp>> findSteamApps() {
        List<SteamApp> steamAppsList = steamAppRepository.findAll();
        if (steamAppsList.size() > 0) {
            log.debug("findSteamApps() :: Retrieved {} app entries from the repository", steamAppsList.size());
            return CompletableFuture.completedFuture(steamAppsList);
        }
        log.debug("findSteamApps() :: Fetching a fresh list from the api service");
        return steamAppsApi.getAppList().thenApply(entityConverter::convert);
    }

    @Override
    public CompletableFuture<SteamAppDetails> findAppDetails(SteamApp app) {
        return steamStorefrontApi.getAppDetails(app.getId()).thenApply(entityConverter::convert);
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
}
