package com.ibasco.sourcebuddy.util.preload;

import com.ibasco.sourcebuddy.model.SteamGamesModel;
import com.ibasco.sourcebuddy.service.SteamService;
import com.ibasco.sourcebuddy.util.PreloadTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SteamAppsPreload extends PreloadTask {

    private static final Logger log = LoggerFactory.getLogger(SteamAppsPreload.class);

    private SteamGamesModel steamAppsModel;

    private SteamService steamService;

    @Override
    public void preload() throws Exception {
        updateMessage("Fetching steam apps");
        indeterminateProgress();
        steamAppsModel.setSteamAppList(steamService.findSteamAppsWithDetails().join());
        updateMessage("Cached a total of %d apps", steamAppsModel.getSteamAppList().size());
    }

    @Autowired
    public void setSteamAppsModel(SteamGamesModel steamAppsModel) {
        this.steamAppsModel = steamAppsModel;
    }

    @Autowired
    public void setSteamService(SteamService steamService) {
        this.steamService = steamService;
    }
}
