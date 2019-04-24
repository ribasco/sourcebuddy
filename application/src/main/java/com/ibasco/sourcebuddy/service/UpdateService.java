package com.ibasco.sourcebuddy.service;

import com.ibasco.sourcebuddy.domain.SteamAppDetails;
import com.ibasco.sourcebuddy.domain.UpdateManifest;
import com.ibasco.sourcebuddy.util.ProgressCallback;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface UpdateService {

    CompletableFuture<Void> invalidateEntries();

    CompletableFuture<Boolean> updateSteamApps(ProgressCallback progressCallback);

    CompletableFuture<UpdateManifest> downloadManifest(String file, ProgressCallback progressCallback);

    CompletableFuture<List<SteamAppDetails>> downloadSteamApps(UpdateManifest manifest, ProgressCallback progressCallback);
}
