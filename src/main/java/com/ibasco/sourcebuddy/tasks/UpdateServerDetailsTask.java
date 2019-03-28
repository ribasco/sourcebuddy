package com.ibasco.sourcebuddy.tasks;

import com.ibasco.sourcebuddy.domain.ServerDetails;
import com.ibasco.sourcebuddy.service.SourceServerService;
import com.ibasco.sourcebuddy.util.ServerDetailsFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@Scope(scopeName = "prototype")
public class UpdateServerDetailsTask extends BaseTask<Void> {

    private static final Logger log = LoggerFactory.getLogger(UpdateServerDetailsTask.class);

    private SourceServerService sourceServerService;

    private List<ServerDetails> servers;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public UpdateServerDetailsTask(List<ServerDetails> serverDetails) {
        if (serverDetails == null)
            throw new IllegalArgumentException("Server list cannot be null");
        this.servers = serverDetails;
    }

    @Autowired
    public void setSourceServerService(SourceServerService sourceServerQueryService) {
        this.sourceServerService = sourceServerQueryService;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " (Total servers: " + servers.size() + ")";
    }

    @Override
    protected Void process() throws Exception {
        updateTitle("Server batch update for 'd' servers", servers.size());

        if (servers.isEmpty()) {
            updateMessage("No available servers to update. Server list is empty");
            log.debug("No available servers to update. Server list is empty");
            return null;
        }

        log.debug("UpdateServerDetailsTask :: Starting batch server details update (Size: {})", servers.size());
        sourceServerService.updateServerDetails(servers, createWorkProgressCallback("Server details update", servers.size()));

        //Update player details (active and non-empty servers only)
        List<ServerDetails> filteredList = servers.stream().filter(ServerDetailsFilter::byActiveServers).filter(ServerDetailsFilter::byNonEmptyServers).collect(Collectors.toList());
        log.debug("UpdateServerDetailsTask :: Starting batch player details update (Size: {})", filteredList.size());
        sourceServerService.updatePlayerDetails(filteredList, createWorkProgressCallback("Player details update", filteredList.size()));

        //Update server rules (active servers only)
        filteredList = servers.stream().filter(ServerDetailsFilter::byActiveServers).collect(Collectors.toList());
        log.debug("UpdateServerDetailsTask :: Starting batch server rules update (Size: {})", filteredList.size());
        sourceServerService.updateServerRules(filteredList, createWorkProgressCallback("Server rules update", filteredList.size()));

        //Save to database
        if (!servers.isEmpty()) {
            log.debug("UpdateServerDetailsTask :: Saving {} entries to database", servers.size());
            sourceServerService.saveServerList(servers);
        }

        return null;
    }
}