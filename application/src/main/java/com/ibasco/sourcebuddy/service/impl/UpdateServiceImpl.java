package com.ibasco.sourcebuddy.service.impl;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ibasco.sourcebuddy.components.HttpDownloader;
import com.ibasco.sourcebuddy.domain.SteamApp;
import com.ibasco.sourcebuddy.domain.SteamAppDetails;
import com.ibasco.sourcebuddy.domain.UpdateManifest;
import com.ibasco.sourcebuddy.exceptions.SignatureVerificationFailed;
import com.ibasco.sourcebuddy.repository.SteamAppDetailsRepository;
import com.ibasco.sourcebuddy.repository.SteamAppsRepository;
import com.ibasco.sourcebuddy.repository.UpdateManifestRepository;
import com.ibasco.sourcebuddy.service.CryptService;
import com.ibasco.sourcebuddy.service.UpdateService;
import com.ibasco.sourcebuddy.util.ProgressCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
public class UpdateServiceImpl implements UpdateService {

    private static final Logger log = LoggerFactory.getLogger(UpdateServiceImpl.class);

    @Value("${app.update-manifest-url}")
    private String updateManifestUrl;

    private HttpDownloader httpDownloader;

    private SteamAppsRepository steamAppsRepository;

    private SteamAppDetailsRepository steamAppDetailsRepository;

    private UpdateManifestRepository updateManifestRepository;

    private Gson gsonProvider;

    private CryptService cryptService;

    private static final String UPDATE_GAMES_FILE = "games.json";

    private static final String UPDATE_MANIFEST_FILE = "update.json";

    @Override
    public CompletableFuture<Void> invalidateEntries() {
        return null;
    }

    @Override
    public CompletableFuture<Boolean> updateSteamApps(ProgressCallback pc) {
        return downloadManifest(UPDATE_GAMES_FILE, pc)
                .thenCompose(manifest -> {
                    updateProgress(pc, "Checking for steam app updates");
                    if (needsUpdate(manifest)) {
                        updateProgress(pc, "Downloading steam app list from site");
                        return downloadSteamApps(manifest, pc)
                                .<List<SteamAppDetails>>thenApply(appDetailsList -> {
                                    //Save manifest to database
                                    updateProgress(pc, "Updating manifest on database");
                                    updateManifestRepository.save(manifest);
                                    log.info("Saved manifest file to database");
                                    return appDetailsList;
                                });
                    }
                    return CompletableFuture.completedFuture(Collections.emptyList());
                }).thenApply(appDetailsList -> {
                    if (!appDetailsList.isEmpty()) {
                        int size = appDetailsList.size();
                        log.info("Updates available: {}", size);
                        for (int i = 0; i < size; i++) {
                            SteamAppDetails appDetails = appDetailsList.get(i);
                            Optional<SteamApp> res = steamAppsRepository.findById(appDetails.getSteamApp().getId());
                            SteamApp app;
                            if (res.isPresent()) {
                                app = res.get();
                            } else {
                                assert appDetails.getSteamApp() != null;
                                app = appDetails.getSteamApp();
                                app.setName(appDetails.getName());
                                app = steamAppsRepository.save(app);
                            }
                            appDetails.setSteamApp(app);
                            updateProgress(pc, i, size, "Saving app: " + appDetails.toString());
                            steamAppDetailsRepository.save(appDetails);
                        }
                        updateProgress(pc, 0, 0, null);
                        return true;
                    }
                    return false;
                });
    }

    private boolean needsUpdate(UpdateManifest siteManifest) {
        Optional<UpdateManifest> res = updateManifestRepository.findById(UPDATE_GAMES_FILE);
        if (res.isEmpty())
            return true;
        UpdateManifest localManifest = res.get();
        return (siteManifest.getLastUpdated().isAfter(localManifest.getLastUpdated()) && !siteManifest.getHash().equalsIgnoreCase(localManifest.getHash())) || steamAppsRepository.count() == 0;
    }

    @Override
    public CompletableFuture<UpdateManifest> downloadManifest(String file, ProgressCallback progressCallback) {
        String url = buildUrl(UPDATE_MANIFEST_FILE);
        log.info("Downloading manifest file from: {}", url);
        updateProgress(progressCallback, "Downloading update manifest");
        try {
            CompletableFuture<List<UpdateManifest>> res = httpDownloader.downloadJson(url, manifestListType(), progressCallback);
            return res.thenApply(manifestList -> manifestList.stream().filter(p -> p.getFile().equalsIgnoreCase(file)).findFirst().orElse(null));
        } catch (IOException e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    public CompletableFuture<List<SteamAppDetails>> downloadSteamApps(UpdateManifest manifest, ProgressCallback progressCallback) {
        String url = buildUrl(manifest.getFile());
        try {
            log.info("Downloading update file: {}", url);
            return httpDownloader.downloadRaw(url, progressCallback)
                    .thenApply(rawData -> verifySignature(manifest, rawData))
                    .thenApply(bytes -> gsonProvider.fromJson(new String(bytes), appDetailsListType()));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private byte[] verifySignature(UpdateManifest manifest, byte[] bytes) {
        String computedHash = cryptService.computeHash(bytes);
        log.info("Signature verification (Expected: {}, Actual: {})", manifest.getHash(), computedHash);
        if (!computedHash.equalsIgnoreCase(manifest.getHash()))
            throw new SignatureVerificationFailed(String.format("Hash signatures does not match (Expected: %s, Actual: %s)", manifest.getHash(), computedHash));
        return bytes;
    }

    private void updateProgress(ProgressCallback progressCallback, String message) {
        updateProgress(progressCallback, -1, -1, message);
    }

    private void updateProgress(ProgressCallback progressCallback, int work, int total, String message) {
        if (progressCallback != null) {
            log.debug(message);
            if (work > total) {
                work = total;
            } else if (work < 0) {
                work = -1;
            }
            progressCallback.onProgress(work, total, message);
        }
    }

    private Type manifestListType() {
        return new TypeToken<ArrayList<UpdateManifest>>() {
        }.getType();
    }

    private Type appDetailsListType() {
        return new TypeToken<ArrayList<SteamAppDetails>>() {
        }.getType();
    }

    private String buildUrl(String file) {
        return String.format("%s/%s", updateManifestUrl, file);
    }

    @Autowired
    public void setHttpDownloader(HttpDownloader httpDownloader) {
        this.httpDownloader = httpDownloader;
    }

    @Autowired
    public void setSteamAppDetailsRepository(SteamAppDetailsRepository steamAppDetailsRepository) {
        this.steamAppDetailsRepository = steamAppDetailsRepository;
    }

    @Autowired
    public void setSteamAppsRepository(SteamAppsRepository steamAppsRepository) {
        this.steamAppsRepository = steamAppsRepository;
    }

    @Autowired
    public void setUpdateManifestRepository(UpdateManifestRepository updateManifestRepository) {
        this.updateManifestRepository = updateManifestRepository;
    }

    @Autowired
    public void setCryptService(CryptService cryptService) {
        this.cryptService = cryptService;
    }

    @Autowired
    public void setGsonProvider(Gson gsonProvider) {
        this.gsonProvider = gsonProvider;
    }
}
