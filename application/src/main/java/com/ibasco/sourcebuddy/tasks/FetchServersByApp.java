package com.ibasco.sourcebuddy.tasks;

import com.ibasco.sourcebuddy.domain.ServerDetails;
import com.ibasco.sourcebuddy.domain.SteamApp;
import com.ibasco.sourcebuddy.service.SourceServerService;
import com.ibasco.sourcebuddy.util.Check;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;
import org.springframework.context.annotation.Scope;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Scope(SCOPE_PROTOTYPE)
public class FetchServersByApp extends BaseTask<Set<ServerDetails>> {

    private static final Logger log = LoggerFactory.getLogger(FetchServersByApp.class);

    private SourceServerService sourceServerService;

    private SteamApp app;

    public FetchServersByApp(SteamApp app) {
        this.app = Check.requireNonNull(app, "App cannot be null");
    }

    @Override
    protected Set<ServerDetails> process() throws Exception {
        updateTitle("Retrieving server entries for %s (%d)", app.getName(), app.getId());
        Set<ServerDetails> serverDetails = new HashSet<>();
        log.debug("FetchServersByApp :: Check if the game already contains server entries in the repository");
        sourceServerService.findServerListByApp(serverDetails, app, createWorkProgressCallback("Checking for existing server entries", -1));
        if (serverDetails.isEmpty()) {
            log.debug("FetchServersByApp :: we do not yet have any entries in the repository, fetch a new one from steam/master server");
            //we do not yet have any entries in the repository, fetch a new one from steam/master server
            int total = sourceServerService.fetchNewServerEntries(app, createWorkProgressCallback("Downloading server entries", -1));
            if (total > 0) {
                log.debug("FetchServersByApp :: Fetching new server list from repository");
                //Fetch the new entries
                total = sourceServerService.findServerListByApp(serverDetails, app, null);
                if (total == 0)
                    throw new IllegalStateException(String.format("No server entries available for app '%s (%d)'", app.getName(), app.getId()));
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
