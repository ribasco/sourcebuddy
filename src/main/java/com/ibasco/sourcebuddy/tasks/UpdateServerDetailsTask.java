package com.ibasco.sourcebuddy.tasks;

import com.ibasco.sourcebuddy.domain.ServerDetails;
import com.ibasco.sourcebuddy.domain.SteamApp;
import com.ibasco.sourcebuddy.model.ServerDetailsModel;
import com.ibasco.sourcebuddy.repository.ServerDetailsRepository;
import com.ibasco.sourcebuddy.service.SourceServerQueryService;
import com.ibasco.sourcebuddy.util.ServerDetailsFilter;
import com.ibasco.sourcebuddy.util.WorkProgressCallback;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Component
@Scope(scopeName = "prototype")
public class UpdateServerDetailsTask extends BaseTask<Void> {

    private static final Logger log = LoggerFactory.getLogger(UpdateServerDetailsTask.class);

    private ServerDetailsRepository serverDetailsRepository;

    private SourceServerQueryService sourceServerQueryService;

    private ServerDetailsModel serverDetailsModel;

    private SteamApp steamApp;

    public UpdateServerDetailsTask(SteamApp steamApp) {
        this.steamApp = steamApp;
    }

    @Override
    protected Void call() throws Exception {
        long startTime = System.currentTimeMillis();

        try {
            ObservableList<ServerDetails> servers = FXCollections.observableArrayList(serverDetailsModel.getServerDetails());

            if (steamApp == null)
                throw new Exception("Steam app not specified");

            updateTitle("Source server details update");
            int total = sourceServerQueryService.populateServerList(servers, this.steamApp, false, createWorkProgressCallback("Obtaining master server list", -1));

            if (total == 0) {
                log.debug("No servers retrieved");
                return null;
            }

            log.debug("Finished retrieving server list (Total: {})", total);

            sourceServerQueryService.updateServerDetails(servers, createWorkProgressCallback("Server details update", servers.size()));

            //Update player details (active and non-empty servers only)
            List<ServerDetails> filteredList = servers.stream().filter(ServerDetailsFilter::byActiveServers).filter(ServerDetailsFilter::byNonEmptyServers).collect(Collectors.toList());
            sourceServerQueryService.updatePlayerDetails(filteredList, createWorkProgressCallback("Player details update", filteredList.size()));

            //Update server rules (active servers only)
            filteredList = servers.stream().filter(ServerDetailsFilter::byActiveServers).collect(Collectors.toList());
            sourceServerQueryService.updateServerRules(filteredList, createWorkProgressCallback("Server rules update", filteredList.size()));

            //Save to database
            log.debug("Saving {} server entries to database", servers.size());
            saveToRepository(servers);

            log.info("DONE (Total time: {} seconds)", Duration.ofMillis(System.currentTimeMillis() - startTime).toSeconds());

            try {
                serverDetailsModel.WRITE_LOCK.lock();
                Platform.runLater(() -> serverDetailsModel.setServerDetails(FXCollections.observableArrayList(servers)));
            } catch (Exception e) {
                throw e;
            } finally {
                serverDetailsModel.WRITE_LOCK.unlock();
            }
        } catch (Exception e) {
            log.error("Update server details task failed", e);
            throw e;
        }

        return null;
    }

    private WorkProgressCallback<ServerDetails> createWorkProgressCallback(String workDesc, final int totalWork) {
        return new WorkProgressCallback<>() {
            final AtomicInteger workCtr = new AtomicInteger();

            final AtomicInteger successCtr = new AtomicInteger();

            final AtomicInteger failedCtr = new AtomicInteger();

            {
                if (totalWork < 0)
                    updateProgress(-1, Long.MIN_VALUE);
            }

            @Override
            public void onProgress(ServerDetails item, Throwable exception) {
                if (exception != null) {
                    failedCtr.incrementAndGet();
                } else {
                    successCtr.incrementAndGet();
                }

                final int work = workCtr.incrementAndGet();

                if (totalWork < 0) {
                    updateMessage(
                            "%s (IP: %s, Processed: %d)",
                            workDesc,
                            item.getAddress().toString().replace("/", ""),
                            work
                    );
                } else {
                    updateProgress(work, totalWork);
                    updateMessage(
                            "%s (Pass: %d, Err: %d, %d/%d (%.2f%%))",
                            workDesc,
                            successCtr.get(),
                            failedCtr.get(),
                            workCtr.get(),
                            totalWork,
                            ((double) work / (double) totalWork) * 100.0d
                    );
                }
            }
        };
    }

    private void saveToRepository(List<ServerDetails> servers) {
        if (servers == null || servers.isEmpty())
            return;
        log.debug("Persisting {} entries to database", servers.size());
        serverDetailsRepository.saveAll(servers);
        serverDetailsRepository.flush();
    }

    public SteamApp getSteamApp() {
        return steamApp;
    }

    @Autowired
    public void setServerDetailsModel(ServerDetailsModel serverDetailsModel) {
        this.serverDetailsModel = serverDetailsModel;
    }

    @Autowired
    public void setServerDetailsRepository(ServerDetailsRepository serverDetailsRepository) {
        this.serverDetailsRepository = serverDetailsRepository;
    }

    @Autowired
    public void setSourceQueryService(SourceServerQueryService sourceServerQueryService) {
        this.sourceServerQueryService = sourceServerQueryService;
    }
}