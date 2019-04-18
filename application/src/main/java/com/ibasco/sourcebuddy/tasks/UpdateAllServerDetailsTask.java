package com.ibasco.sourcebuddy.tasks;

import com.ibasco.sourcebuddy.domain.ServerDetails;
import com.ibasco.sourcebuddy.service.SourceServerService;
import com.ibasco.sourcebuddy.util.ServerDetailsFilter;
import com.ibasco.sourcebuddy.util.WorkProgressCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

@Scope("prototype")
public class UpdateAllServerDetailsTask extends BaseTask<Void> {

    private static final Logger log = LoggerFactory.getLogger(UpdateAllServerDetailsTask.class);

    private SourceServerService sourceServerService;

    private List<ServerDetails> servers;

    private List<CompletableFuture<?>> taskFutures = new ArrayList<>();

    public UpdateAllServerDetailsTask(List<ServerDetails> serverDetails) {
        if (serverDetails == null)
            throw new IllegalArgumentException("Server list cannot be null");
        this.servers = serverDetails;
    }

    @Override
    protected void cancelled() {
        for (CompletableFuture<?> cf : taskFutures) {
            log.debug("Cancelling future task: {}", cf);
            cf.cancel(true);
        }
    }

    @Override
    protected Void process() throws Exception {
        updateTitle("Server batch update for '%d' servers", servers.size());

        try {
            if (servers.isEmpty()) {
                updateMessage("No available servers to update. Server list is empty");
                log.debug("No available servers to update. Server list is empty");
                return null;
            }
            log.debug("UpdateServerDetailsTask :: Starting batch server details update (Size: {})", servers.size());
            runFutureTask(sourceServerService::updateServerDetails, "Updating server details", servers, servers.size()).join();

            //Update player details (active and non-empty servers only)
            List<ServerDetails> filteredList = servers.stream().filter(ServerDetailsFilter::byActiveServers).filter(ServerDetailsFilter::byNonEmptyServers).collect(Collectors.toList());

            log.debug("UpdateServerDetailsTask :: Starting batch player details update (Size: {})", filteredList.size());
            runFutureTask(sourceServerService::updatePlayerDetails, "Updating player details", filteredList, filteredList.size()).join();

            //Update server rules (active servers only)
            filteredList = servers.stream().filter(ServerDetailsFilter::byActiveServers).collect(Collectors.toList());
            log.debug("UpdateServerDetailsTask :: Starting batch server rules update (Size: {})", filteredList.size());
            runFutureTask(sourceServerService::updateServerRules, "Updating server rules", filteredList, filteredList.size()).join();

            //Save to database
            if (!servers.isEmpty()) {
                log.debug("UpdateServerDetailsTask :: Saving {} entries to database", servers.size());
                sourceServerService.saveServerList(servers);
            }
        } finally {
            log.debug("Done");
        }
        return null;
    }

    private CompletableFuture<Void> runFutureTask(BiFunction<List<ServerDetails>, WorkProgressCallback<ServerDetails>, CompletableFuture<Void>> action, String desc, List<ServerDetails> serverList, int workSize) {
        CompletableFuture<Void> cf = action.apply(serverList, createWorkProgressCallback(desc, workSize));
        taskFutures.add(cf);
        cf.join();
        return cf;
    }

    @Autowired
    public void setSourceServerService(SourceServerService sourceServerQueryService) {
        this.sourceServerService = sourceServerQueryService;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " (Total servers: " + servers.size() + ")";
    }



}