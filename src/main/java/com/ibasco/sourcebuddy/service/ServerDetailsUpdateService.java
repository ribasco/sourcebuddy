package com.ibasco.sourcebuddy.service;

import com.ibasco.agql.protocols.valve.source.query.client.SourceQueryClient;
import com.ibasco.sourcebuddy.dao.SourceServerDetailsDao;
import com.ibasco.sourcebuddy.enums.OperatingSystem;
import com.ibasco.sourcebuddy.enums.ServerStatus;
import com.ibasco.sourcebuddy.model.SourceServerDetails;
import javafx.application.Platform;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

public class ServerDetailsUpdateService extends ScheduledService<Void> {

    private static final Logger log = LoggerFactory.getLogger(ServerDetailsUpdateService.class);

    private ListProperty<SourceServerDetails> serverList = new SimpleListProperty<>();

    private SourceQueryClient serverQueryClient;

    private ObjectProperty<Duration> duration = new SimpleObjectProperty<>();

    private final AtomicInteger maxWork = new AtomicInteger();

    private final AtomicInteger work = new AtomicInteger();

    private long startTime = 0;

    private SourceServerDetailsDao sourceServerDao;

    public ServerDetailsUpdateService() {
    }

    @Override
    protected Task<Void> createTask() {
        return new Task<>() {
            @Override
            protected Void call() {
                List<SourceServerDetails> serverInfoList = getServerList();

                if (serverInfoList.size() <= 0)
                    return null;

                List<SourceServerDetails> servers = new ArrayList<>(serverInfoList);

                maxWork.set(servers.size());

                List<CompletableFuture<Void>> allFutures = new ArrayList<>();

                startTime = System.currentTimeMillis();

                log.info("Running update service for {} servers", servers.size());

                servers.parallelStream().forEach(info -> {
                    log.info("Querying {}", info.getName());
                    CompletableFuture<Void> infoFuture = serverQueryClient.getServerInfo(info.getAddress()).thenAccept(server -> {
                        info.setName(server.getName());
                        info.setServerTags(server.getServerTags());
                        info.setPlayerCount(server.getNumOfPlayers());
                        info.setMaxPlayerCount(server.getMaxPlayers());
                        info.setGameDirectory(server.getGameDirectory());
                        info.setDescription(server.getGameDescription());
                        info.setGameId(server.getGameId());
                        info.setMapName(server.getMapName());
                        info.setGameId(server.getGameId());
                        info.setAppId((int) server.getAppId());
                        info.setOperatingSystem(OperatingSystem.valueOf(server.getOperatingSystem()));
                        info.setVersion(server.getGameVersion());
                        info.setStatus(ServerStatus.ACTIVE);
                        info.setLastUpdate(LocalDateTime.now());
                    }).whenComplete((ignored, ex) -> {
                        if (ex != null) {
                            log.error("Error {}", ex);
                            info.setStatus(ServerStatus.INACTIVE);
                            return;
                        }
                        updateProgress(ignored, null);
                    });

                    /*CompletableFuture<Void> playerFuture = serverQueryClient.getPlayersCached(info.getAddress())
                            .thenApply(lst -> lst.stream().map(SourcePlayerInfo::new).collect(Collectors.toCollection(FXCollections::observableArrayList)))
                            .thenAccept(info::setPlayers)
                            .whenComplete(this::updateProgress);

                    CompletableFuture<Void> rulesFuture = serverQueryClient.getServerRulesCached(info.getAddress())
                            .thenApply(FXCollections::observableMap)
                            .thenAccept(info::setRules)
                            .whenComplete(this::updateProgress);

                    allFutures.add(playerFuture);
                    allFutures.add(rulesFuture);
                    */
                    allFutures.add(infoFuture);
                });

                log.info("Waiting for completion of {} tasks", allFutures.size());
                CompletableFuture.allOf(allFutures.toArray(new CompletableFuture[0])).join();

                duration.set(Duration.ofMillis(System.currentTimeMillis()));

                log.info("DONE");
                return null;
            }

            private void updateProgress(Void aVoid, Throwable throwable) {
                super.updateProgress(work.incrementAndGet(), maxWork.get());
                Platform.runLater(() -> duration.set(Duration.ofMillis(System.currentTimeMillis() - startTime)));
            }
        };
    }

    public Duration getDuration() {
        return duration.get();
    }

    public ObjectProperty<Duration> durationProperty() {
        return duration;
    }

    public ObservableList<SourceServerDetails> getServerList() {
        return serverList.get();
    }

    public ListProperty<SourceServerDetails> serverListProperty() {
        return serverList;
    }

    public void setServerList(ObservableList<SourceServerDetails> serverList) {
        this.serverList.set(serverList);
    }

    @Autowired
    public void setSourceServerDao(SourceServerDetailsDao sourceServerDao) {
        this.sourceServerDao = sourceServerDao;
    }

    @Autowired
    public void setServerQueryClient(SourceQueryClient serverQueryClient) {
        this.serverQueryClient = serverQueryClient;
    }
}
