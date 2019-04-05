package com.ibasco.sourcebuddy.tasks;

import com.ibasco.sourcebuddy.domain.ServerDetails;
import com.ibasco.sourcebuddy.domain.SteamApp;
import com.ibasco.sourcebuddy.model.ServerDetailsModel;
import com.ibasco.sourcebuddy.service.SourceServerService;
import com.ibasco.sourcebuddy.util.Check;
import javafx.collections.FXCollections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;

import java.util.ArrayList;
import java.util.List;

@Scope("prototype")
public class SwitchGameTask extends BaseTask<Void> {

    private static final Logger log = LoggerFactory.getLogger(SwitchGameTask.class);

    private SourceServerService sourceServerService;

    private ServerDetailsModel serverDetailsModel;

    private SteamApp app;

    public SwitchGameTask(SteamApp app) {
        this.app = Check.requireNonNull(app, "App cannot be null");
    }

    @Override
    protected Void process() throws Exception {
        List<ServerDetails> serverDetails = new ArrayList<>();
        //TODO: Once the server details have been retrieved from the repository, return immediately

        log.debug("SwitchGameTask :: Check if the game already contains server entries in the repository");
        sourceServerService.findServerListByApp(serverDetails, app, createWorkProgressCallback("Checking for existing server entries", -1)).join();

        if (serverDetails.isEmpty()) {
            log.debug("SwitchGameTask :: we do not yet have any entries in the repository, fetch a new one from steam/master server");
            //we do not yet have any entries in the repository, fetch a new one from steam/master server
            sourceServerService.fetchNewServerEntries(app, createWorkProgressCallback("Fetching new server entries from steam/master", -1));

            log.debug("Fetching app list from repository after fresh retrieval");
            //Fetch the new entries
            sourceServerService.findServerListByApp(serverDetails, app, null);
        }

        if (serverDetails.size() > 0) {
            //Update model
            log.debug("Setting server model list to {} entries", serverDetails.size());
            serverDetailsModel.setServerDetails(FXCollections.observableArrayList(serverDetails));

            //log.debug("SwitchGameTask :: updating server details for {} entries", serverDetails.size());
            //Once we have fetched the server entries, update the details
            //sourceServerService.updateAllServerDetails(serverDetailsModel.getServerDetails(), null).whenComplete((aVoid, throwable) -> serverDetailsModel.setServerListUpdating(false));
        }

        log.debug("SwitchGameTask :: DONE");
        return null;
    }

    @Autowired
    public void setSourceServerService(SourceServerService sourceServerService) {
        this.sourceServerService = sourceServerService;
    }

    @Autowired
    public void setServerDetailsModel(ServerDetailsModel serverDetailsModel) {
        this.serverDetailsModel = serverDetailsModel;
    }
}
