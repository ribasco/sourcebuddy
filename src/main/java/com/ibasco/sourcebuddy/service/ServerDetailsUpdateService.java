package com.ibasco.sourcebuddy.service;

import com.ibasco.agql.core.exceptions.ReadTimeoutException;
import com.ibasco.agql.protocols.valve.source.query.client.SourceQueryClient;
import com.ibasco.agql.protocols.valve.source.query.pojos.SourcePlayer;
import com.ibasco.agql.protocols.valve.source.query.pojos.SourceServer;
import com.ibasco.agql.protocols.valve.steam.master.MasterServerFilter;
import com.ibasco.agql.protocols.valve.steam.master.client.MasterServerQueryClient;
import com.ibasco.agql.protocols.valve.steam.master.enums.MasterServerRegion;
import com.ibasco.agql.protocols.valve.steam.master.enums.MasterServerType;
import com.ibasco.sourcebuddy.dao.SourceServerDetailsDao;
import com.ibasco.sourcebuddy.entities.SourcePlayerInfo;
import com.ibasco.sourcebuddy.entities.SourceServerDetails;
import com.ibasco.sourcebuddy.enums.OperatingSystem;
import com.ibasco.sourcebuddy.enums.ServerStatus;
import com.ibasco.sourcebuddy.model.ServerDetailsModel;
import com.ibasco.sourcebuddy.util.ServerDetailsFilter;
import com.ibasco.sourcebuddy.util.ThreadUtil;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class ServerDetailsUpdateService extends ScheduledService<Void> {

    private static final Logger log = LoggerFactory.getLogger(ServerDetailsUpdateService.class);

    private SourceQueryClient serverQueryClient;

    private ObjectProperty<Duration> duration = new SimpleObjectProperty<>();

    private final AtomicInteger workDone = new AtomicInteger();

    private final AtomicInteger maxWork = new AtomicInteger();

    private long startTime = 0;

    private SourceServerDetailsDao sourceServerDao;

    private ServerDetailsModel serverDetailsModel;

    private MasterServerQueryClient masterQueryClient;

    public ServerDetailsUpdateService() {
    }

    private class UpdateServerPropertiesTask extends Task<Void> {

        @Override
        protected Void call() throws Exception {
            startTime = System.currentTimeMillis();

            updateTitle("Fetch cached server entries");
            List<SourceServerDetails> serverInfoList = retrieveServerInfoList();

            if (serverInfoList == null || serverInfoList.size() <= 0) {
                Platform.runLater(() -> duration.set(Duration.ofMillis(System.currentTimeMillis() - startTime)));
                return null;
            }

            List<SourceServerDetails> servers = new ArrayList<>(serverInfoList);

            log.info("UPDATE SERVERS: Running update service for {} servers", servers.size());

            try {
                updateServerDetails(servers);
                updatePlayerDetails(servers);
                updateServerRules(servers);
            } catch (Exception e) {
                log.error("Update server(s) failed", e);
            } finally {
                workDone.set(0);
                maxWork.set(0);
                super.updateProgress(0, 0);
            }

            log.info("UPDATE SERVERS: Update Complete! Total Time: {} seconds", Duration.ofMillis(System.currentTimeMillis() - startTime).toSeconds());

            try {
                log.info("Persisting updates to database");

                sourceServerDao.saveAll(servers);
                sourceServerDao.flush();
            } catch (Throwable e) {
                log.error("Error persisting updates to database", e);
            }

            Platform.runLater(() -> duration.set(Duration.ofMillis(System.currentTimeMillis() - startTime)));
            log.info("DONE");

            return null;
        }

        private List<SourceServerDetails> retrieveServerInfoList() {
            try {
                //check if we have existing entries in the cache
                if (serverDetailsModel.getServerDetails().size() > 0) {
                    updateMsg("Found %d cached server entries", serverDetailsModel.getServerDetails().size());
                    return serverDetailsModel.getServerDetails();
                }

                List<SourceServerDetails> sourceServerDetails = sourceServerDao.findAll();

                //check database
                updateMsg("Checking server entries from database");
                if (sourceServerDetails.size() > 0) {
                    serverDetailsModel.getServerDetails().addAll(sourceServerDetails);
                    updateMsg("Found %d server entries from the database", serverDetailsModel.getServerDetails().size());
                    return sourceServerDetails;
                }

                //no server entries available, time to populate
                MasterServerFilter filter = MasterServerFilter.create().dedicated(true).appId(550);
                updateMsg("Fetching server entries from master (Filter: %s)", filter.toString());
                masterQueryClient.getServerList(MasterServerType.SOURCE, MasterServerRegion.REGION_ALL, filter, (serverAddress, senderAddress, ex) -> {
                    if (ex != null) {
                        updateMsg("Error thrown during master server query", ex.getMessage());
                        return;
                    }
                    updateMsg("Adding IP: %s:%d", serverAddress.getAddress().getHostAddress(), serverAddress.getPort());
                    serverDetailsModel.getServerDetails().add(new SourceServerDetails(serverAddress.getAddress().getHostAddress(), serverAddress.getPort()));
                }).join();

                updateMsg("Got total of %d servers from the master server", sourceServerDetails.size());
                log.info("Added a total of {} entries in model collection", serverDetailsModel.getServerDetails().size());
                return sourceServerDetails;
            } catch (Exception e) {
                log.error("Error thrown while retriving server info list", e);
            }
            return null;
        }

        private void updateServerDetails(List<SourceServerDetails> servers) throws InterruptedException {
            CountDownLatch latch = new CountDownLatch(servers.size());

            workDone.set(0);
            maxWork.set(servers.size());

            log.info("Server details update - Start (Total: {}, Total Queries: {})", servers.size(), maxWork.get());
            servers.parallelStream().forEach(info -> {
                if (info.getAddress() == null) {
                    log.warn("Address is null for {}", info.getName());
                    return;
                }
                updateMsg("Retrieving server info for %s", info.getAddress());
                serverQueryClient.getServerInfo(info.getAddress())
                        .whenComplete((server, ex) -> {
                            if (ex != null) {
                                if (ex.getCause() instanceof ReadTimeoutException) {
                                    info.setStatus(ServerStatus.TIMED_OUT);
                                } else {
                                    info.setStatus(ServerStatus.ERRORED);
                                    log.error("Error on getServerInfo({})", info.getAddress());
                                }
                                info.setLastUpdate(LocalDateTime.now());
                                updateProgress();
                                latch.countDown();
                                return;
                            }
                            //Update server details
                            updateSourceDetails(info, server);
                            updateProgress();
                            latch.countDown();
                        });
                ThreadUtil.sleepUninterrupted(1);
            });

            log.info("Server details update - Waiting for completion (Total: {})", maxWork.get());
            latch.await();
        }

        private void updatePlayerDetails(List<SourceServerDetails> servers) throws InterruptedException {
            List<SourceServerDetails> filteredList = servers.stream()
                    .filter(ServerDetailsFilter::byActiveServers)
                    .filter(ServerDetailsFilter::byNonEmptyServers)
                    .collect(Collectors.toList());

            CountDownLatch latch = new CountDownLatch(filteredList.size());
            initProgress(filteredList.size());

            log.info("Player details update - Start (Total: {})", filteredList.size());
            filteredList.parallelStream().forEach(info -> {
                serverQueryClient.getPlayers(info.getAddress()).whenComplete((playerList, ex) -> {
                    if (ex != null) {
                        updateProgress();
                        latch.countDown();
                        return;
                    }
                    info.setPlayers(toSourcePlayerInfoList(playerList));
                    updateProgress();
                    latch.countDown();
                });
                ThreadUtil.sleepUninterrupted(1);
            });

            log.info("Player details update - Waiting (Total: {})", maxWork.get());
            latch.await();
        }

        private void updateServerRules(List<SourceServerDetails> servers) throws InterruptedException {
            List<SourceServerDetails> filteredList = servers.stream()
                    .filter(ServerDetailsFilter::byActiveServers)
                    .collect(Collectors.toList());

            CountDownLatch latch = new CountDownLatch(filteredList.size());
            initProgress(filteredList.size());

            log.info("Server rules update - Start (Total: {})", filteredList.size());
            filteredList.parallelStream().forEach(info -> {
                serverQueryClient.getServerRules(info.getAddress()).whenComplete((rulesMap, ex) -> {
                    if (ex == null)
                        info.setRules(FXCollections.observableMap(rulesMap));
                    updateProgress();
                    latch.countDown();
                });
            });
            log.info("Server rules update - Waiting (Total: {})", maxWork.get());
            latch.await();
        }

        private void initProgress(int total) {
            workDone.set(0);
            maxWork.set(total);
        }

        void updateMsg(String message, Object... args) {
            String formattedMsg = String.format(message, args);
            log.debug(formattedMsg);
            super.updateMessage(formattedMsg);
        }

        private void updateProgress() {
            super.updateProgress(workDone.incrementAndGet(), maxWork.get());
            Platform.runLater(() -> duration.set(Duration.ofMillis(System.currentTimeMillis() - startTime)));
        }
    }

    private ObservableList<SourcePlayerInfo> toSourcePlayerInfoList(List<SourcePlayer> sourcePlayers) {
        return sourcePlayers.stream().map(SourcePlayerInfo::new).collect(Collectors.toCollection(FXCollections::observableArrayList));
    }

    @Override
    protected Task<Void> createTask() {
        return new UpdateServerPropertiesTask();
    }

    private void updateSourceDetails(SourceServerDetails target, SourceServer source) {
        target.setName(source.getName());
        target.setServerTags(source.getServerTags());
        target.setPlayerCount(source.getNumOfPlayers());
        target.setMaxPlayerCount(source.getMaxPlayers());
        target.setGameDirectory(source.getGameDirectory());
        target.setDescription(source.getGameDescription());
        target.setGameId(source.getGameId());
        target.setMapName(source.getMapName());
        target.setGameId(source.getGameId());
        target.setAppId((int) source.getAppId());
        target.setOperatingSystem(OperatingSystem.valueOf(source.getOperatingSystem()));
        target.setVersion(source.getGameVersion());
        target.setStatus(ServerStatus.ACTIVE);
        target.setLastUpdate(LocalDateTime.now());
    }

    public Duration getDuration() {
        return duration.get();
    }

    public ObjectProperty<Duration> durationProperty() {
        return duration;
    }

    @Autowired
    public void setServerDetailsModel(ServerDetailsModel serverDetailsModel) {
        this.serverDetailsModel = serverDetailsModel;
    }

    @Autowired
    public void setSourceServerDao(SourceServerDetailsDao sourceServerDao) {
        this.sourceServerDao = sourceServerDao;
    }

    @Autowired
    public void setServerQueryClient(SourceQueryClient serverQueryClient) {
        this.serverQueryClient = serverQueryClient;
    }

    @Autowired
    public void setMasterQueryClient(MasterServerQueryClient masterQueryClient) {
        this.masterQueryClient = masterQueryClient;
    }
}
