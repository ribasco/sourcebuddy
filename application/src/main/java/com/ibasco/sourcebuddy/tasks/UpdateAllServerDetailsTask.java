package com.ibasco.sourcebuddy.tasks;

import com.ibasco.sourcebuddy.domain.ServerDetails;
import com.ibasco.sourcebuddy.enums.ServerStatus;
import com.ibasco.sourcebuddy.model.ServerFilterModel;
import com.ibasco.sourcebuddy.service.SourceServerService;
import com.ibasco.sourcebuddy.util.Check;
import com.ibasco.sourcebuddy.util.ServerDetailsFilter;
import com.ibasco.sourcebuddy.util.WorkProgressCallback;
import javafx.application.Platform;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Scope("prototype")
public class UpdateAllServerDetailsTask extends BaseTask<Void> {

    private static final Logger log = LoggerFactory.getLogger(UpdateAllServerDetailsTask.class);

    private SourceServerService sourceServerService;

    private List<ServerDetails> servers;

    private ServerFilterModel serverFilterModel;

    public UpdateAllServerDetailsTask(final List<ServerDetails> serverDetails) {
        this.servers = Check.requireNonNull(serverDetails, "Server list cannot be null");
    }

    @Override
    protected Void process() throws Exception {
        updateTitle("Server batch update for '%d' servers", servers.size());

        try {
            if (servers.isEmpty()) {
                updateMessage("No available servers to update. Server list is empty");
                log.debug("UpdateAllServerDetailsTask :: No available servers to update. Server list is empty");
                return null;
            }

            BlockingDeque<String> mapQueue = new LinkedBlockingDeque<>(30);
            serverFilterModel.getMaps().clear();
            log.debug("UpdateAllServerDetailsTask :: Starting batch server details update (Size: {})", servers.size());

            sourceServerService.updateServerDetails(servers, new WorkProgressCallback<>() {
                private final AtomicInteger counter = new AtomicInteger();

                @Override
                public void onProgress(ServerDetails details, Throwable ex) {
                    if (details == null)
                        return;
                    synchronized (details) {
                        int count = counter.incrementAndGet();
                        if (ex != null)
                            return;

                        //Update maps
                        if (ServerStatus.ACTIVE.equals(details.getStatus()) && details.getMapName() != null) {
                            if (mapQueue.remainingCapacity() <= 0) {
                                Platform.runLater(() -> mapQueue.drainTo(serverFilterModel.getMaps()));
                            } else {
                                if (!StringUtils.isBlank(details.getMapName())) {
                                    mapQueue.offer(details.getMapName().toLowerCase());
                                }
                            }
                        }
                        updateMessage("Updating server '%s' (Total: %d)", details.getAddress(), count);
                        updateProgress(count, servers.size());
                    }
                }
            });

            if (mapQueue.size() > 0) {
                Platform.runLater(() -> mapQueue.drainTo(serverFilterModel.getMaps()));
            }

            //Update player details (active and non-empty servers only)
            List<ServerDetails> filteredList = servers.stream().filter(ServerDetailsFilter::byActiveServers).filter(ServerDetailsFilter::byNonEmptyServers).collect(Collectors.toList());
            log.debug("UpdateAllServerDetailsTask :: Starting batch player details update (Size: {})", filteredList.size());
            sourceServerService.updatePlayerDetails(filteredList, createWorkProgressCallback("Updating player details", filteredList.size()));

            //Update server rules (active servers only)
            filteredList = servers.stream().filter(ServerDetailsFilter::byActiveServers).collect(Collectors.toList());
            log.debug("UpdateAllServerDetailsTask :: Starting batch server rules update (Size: {})", filteredList.size());
            sourceServerService.updatePlayerDetails(filteredList, createWorkProgressCallback("Updating server rules", filteredList.size()));

            //Save to database
            if (!servers.isEmpty()) {
                log.debug("UpdateAllServerDetailsTask :: Saving {} entries to database", servers.size());
                sourceServerService.save(servers);
            }
        } finally {
            log.debug("UpdateAllServerDetailsTask :: Done");
        }
        return null;
    }

    @Autowired
    public void setSourceServerService(SourceServerService sourceServerQueryService) {
        this.sourceServerService = sourceServerQueryService;
    }

    @Autowired
    public void setServerFilterModel(ServerFilterModel serverFilterModel) {
        this.serverFilterModel = serverFilterModel;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " (Total servers: " + servers.size() + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !getClass().equals(o.getClass())) return false;
        UpdateAllServerDetailsTask that = (UpdateAllServerDetailsTask) o;
        return servers.equals(that.servers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(servers, getClass());
    }
}