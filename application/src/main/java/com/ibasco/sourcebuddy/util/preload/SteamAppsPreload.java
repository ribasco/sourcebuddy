package com.ibasco.sourcebuddy.util.preload;

import com.ibasco.sourcebuddy.annotations.PreloadOrder;
import com.ibasco.sourcebuddy.model.SteamAppsModel;
import com.ibasco.sourcebuddy.service.SteamService;
import com.ibasco.sourcebuddy.service.UpdateService;
import com.ibasco.sourcebuddy.util.PreloadTask;
import javafx.collections.FXCollections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@PreloadOrder(1)
public class SteamAppsPreload extends PreloadTask {

    private static final Logger log = LoggerFactory.getLogger(SteamAppsPreload.class);

    private SteamAppsModel steamAppsModel;

    private UpdateService updateService;

    private SteamService steamService;

    @Override
    public void preload() throws Exception {
        updateMessage("Fetching steam apps");
        updateService.updateSteamApps(this::updateProgress)
                .thenCompose(updated -> steamService.findSteamAppsFromRepo())
                .thenApply(FXCollections::observableArrayList)
                .thenAccept(steamAppsModel::setSteamAppList)
                .join();
        updateMessage("Cached a total of %d apps", steamAppsModel.getSteamAppList().size());
    }

    private void updateProgress(int work, int total, String message) {
        updateProgress(work, total);
        updateMessage(message);
    }

    @Autowired
    public void setSteamAppsModel(SteamAppsModel steamAppsModel) {
        this.steamAppsModel = steamAppsModel;
    }

    @Autowired
    public void setUpdateService(UpdateService updateService) {
        this.updateService = updateService;
    }

    @Autowired
    public void setSteamService(SteamService steamService) {
        this.steamService = steamService;
    }
}
