package com.ibasco.sourcebuddy.tasks;

import com.ibasco.sourcebuddy.domain.ServerDetails;
import com.ibasco.sourcebuddy.domain.SteamApp;
import com.ibasco.sourcebuddy.service.SourceServerService;
import com.ibasco.sourcebuddy.util.Check;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Scope("prototype")
public class FetchServersByApp extends BaseTask<List<ServerDetails>> {

    private static final Logger log = LoggerFactory.getLogger(FetchServersByApp.class);

    private SourceServerService sourceServerService;

    private SteamApp app;

    public FetchServersByApp(SteamApp app) {
        this.app = Check.requireNonNull(app, "App cannot be null");
    }

    @Override
    protected List<ServerDetails> process() throws Exception {
        List<ServerDetails> serverDetails = new ArrayList<>();
        log.debug("FetchServersByApp :: Check if the game already contains server entries in the repository");
        sourceServerService.findServerListByApp(serverDetails, app, createWorkProgressCallback("Checking for existing server entries", -1)).join();
        if (serverDetails.isEmpty()) {
            log.debug("FetchServersByApp :: we do not yet have any entries in the repository, fetch a new one from steam/master server");
            //we do not yet have any entries in the repository, fetch a new one from steam/master server
            long total = sourceServerService.fetchNewServerEntries(app, createWorkProgressCallback("Fetching new server entries from steam/master", serverDetails.size()));

            if (total > 0) {
                log.debug("FetchServersByApp :: Fetching new server list from repository");
                //Fetch the new entries
                sourceServerService.findServerListByApp(serverDetails, app, null).join();
            } else {
                log.warn("FetchServersByApp :: Nothing was fetched");
            }
        }
        return serverDetails;
    }

    @Autowired
    public void setSourceServerService(SourceServerService sourceServerService) {
        this.sourceServerService = sourceServerService;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FetchServersByApp that = (FetchServersByApp) o;
        return Objects.equals(app, that.app);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClass(), app);
    }
}
