package com.ibasco.sourcebuddy.service.impl;

import com.ibasco.agql.core.exceptions.ReadTimeoutException;
import com.ibasco.agql.core.utils.ServerFilter;
import com.ibasco.agql.protocols.valve.source.query.client.SourceQueryClient;
import com.ibasco.agql.protocols.valve.source.query.pojos.SourceServer;
import com.ibasco.agql.protocols.valve.steam.master.client.MasterServerQueryClient;
import com.ibasco.agql.protocols.valve.steam.master.enums.MasterServerRegion;
import com.ibasco.agql.protocols.valve.steam.master.enums.MasterServerType;
import com.ibasco.sourcebuddy.components.EntityMapper;
import static com.ibasco.sourcebuddy.components.GuiHelper.invokeIfPresent;
import com.ibasco.sourcebuddy.domain.Country;
import com.ibasco.sourcebuddy.domain.ServerDetails;
import com.ibasco.sourcebuddy.domain.SteamApp;
import com.ibasco.sourcebuddy.enums.OperatingSystem;
import com.ibasco.sourcebuddy.enums.ServerStatus;
import com.ibasco.sourcebuddy.repository.CountryRepository;
import com.ibasco.sourcebuddy.repository.ServerDetailsRepository;
import com.ibasco.sourcebuddy.service.GeoIpService;
import com.ibasco.sourcebuddy.service.SourceServerService;
import com.ibasco.sourcebuddy.service.SteamService;
import com.ibasco.sourcebuddy.util.ServerDetailsFilter;
import com.ibasco.sourcebuddy.util.ThreadUtil;
import com.ibasco.sourcebuddy.util.WorkProgressCallback;
import javafx.collections.FXCollections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Transactional
public class SourceServerServiceImpl implements SourceServerService {

    private static final Logger log = LoggerFactory.getLogger(SourceServerServiceImpl.class);

    private final ServerFilter filter = ServerFilter.create().dedicated(true);

    private SourceQueryClient sourceServerQueryClient;

    private GeoIpService geoIpService;

    private CountryRepository countryRepository;

    private MasterServerQueryClient masterServerQueryClient;

    private ServerDetailsRepository serverDetailsRepository;

    private SteamService steamQueryService;

    private EntityMapper entityMapper;

    @Override
    public CompletableFuture<Void> updateAllServerDetails(List<ServerDetails> servers, WorkProgressCallback<ServerDetails> callback) throws InterruptedException {
        if (servers.isEmpty()) {
            log.warn("No available servers to update. Server list is empty");
            return CompletableFuture.completedFuture(null);
        }

        try {
            log.info("updateAllServerDetails()  :: Starting batch server details update (Size: {})", servers.size());
            updateServerDetails(servers, callback);

            //Update player details (active and non-empty servers only)
            List<ServerDetails> filteredList = servers.stream().filter(ServerDetailsFilter::byActiveServers).filter(ServerDetailsFilter::byNonEmptyServers).collect(Collectors.toList());
            log.info("updateAllServerDetails() :: Starting batch player details update (Size: {})", filteredList.size());
            updatePlayerDetails(filteredList, callback);

            //Update server rules (active servers only)
            filteredList = servers.stream().filter(ServerDetailsFilter::byActiveServers).collect(Collectors.toList());
            log.info("updateAllServerDetails() :: Starting batch server rules update (Size: {})", filteredList.size());
            updateServerRules(filteredList, callback);

            //Save to database
            if (!servers.isEmpty()) {
                log.info("updateAllServerDetails() :: Saving {} entries to database", servers.size());
                saveServerList(servers);
                log.info("updateAllServerDetails() :: Successfully saved {} entries to database", servers.size());
            }
        } catch (Throwable e) {
            return CompletableFuture.failedFuture(e);
        }

        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void updateServerDetails(List<ServerDetails> servers, WorkProgressCallback<ServerDetails> callback) throws InterruptedException {
        log.info("updateServerDetails() :: Running server details update for {} servers", servers.size());
        CountDownLatch latch = new CountDownLatch(servers.size());
        servers.parallelStream().forEach(target -> {
            sourceServerQueryClient.getServerInfo(target.getAddress())
                    .whenComplete((server, ex) -> {
                        try {
                            if (ex == null) {
                                copySourceServerDetails(target, server);
                            } else {
                                if (ex.getCause() instanceof ReadTimeoutException) {
                                    target.setStatus(ServerStatus.TIMED_OUT);
                                } else {
                                    log.debug("updateServerDetails() : ERROR", ex);
                                    target.setStatus(ServerStatus.ERRORED);
                                }
                            }
                            invokeIfPresent(callback, target, ex);
                        } finally {
                            latch.countDown();
                        }
                    });
            ThreadUtil.sleepUninterrupted(5);
        });

        //Update country details
        log.info("updateServerEntries() :: Updating country details for new {} server entries", servers.size());
        servers.forEach(this::updateCountryDetails);

        log.info("updateServerDetails() :: Waiting for completion of server details update");
        latch.await();
    }

    @Override
    public void updatePlayerDetails(List<ServerDetails> servers, WorkProgressCallback<ServerDetails> callback) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(servers.size());
        log.info("updatePlayerDetails() :: Running player details update for {} active and non-empty servers", servers.size());

        servers.parallelStream().forEach(target -> {
            sourceServerQueryClient.getPlayers(target.getAddress()).whenComplete((playerList, ex) -> {
                try {
                    if (ex == null)
                        target.setPlayers(FXCollections.observableArrayList(entityMapper.map(playerList)));
                    invokeIfPresent(callback, target, ex);
                } finally {
                    latch.countDown();
                }
            });
            ThreadUtil.sleepUninterrupted(5);
        });
        log.info("updatePlayerDetails() :: Waiting for completion of server players update");
        latch.await();
    }

    @Override
    public void updateServerRules(List<ServerDetails> servers, WorkProgressCallback<ServerDetails> callback) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(servers.size());
        log.info("updateServerRules() :: Running server rules update for {} active servers", servers.size());
        servers.parallelStream().forEach(info -> {
            sourceServerQueryClient.getServerRules(info.getAddress()).whenComplete((rulesMap, ex) -> {
                try {
                    if (ex == null)
                        info.setRules(FXCollections.observableMap(rulesMap));
                    invokeIfPresent(callback, info, ex);
                } finally {
                    latch.countDown();
                }
            });
            ThreadUtil.sleepUninterrupted(5);
        });
        log.info("updateServerRules() :: Waiting for completion of server rulse update");
        latch.await();
    }

    @Override
    public void updateCountryDetails(ServerDetails details) {
        if (details.getCountry() != null)
            return;
        //Add country information
        com.maxmind.geoip2.record.Country countryInfo = geoIpService.findCountry(details.getAddress());
        if (countryInfo != null && countryInfo.getIsoCode() != null) {
            Optional<Country> country = countryRepository.findById(countryInfo.getIsoCode());
            if (country.isPresent()) {
                details.setCountry(country.get());
            } else {
                Country newCountryInfo = new Country();
                newCountryInfo.setCountryCode(countryInfo.getIsoCode());
                newCountryInfo.setCountryName(countryInfo.getName());
                countryRepository.save(newCountryInfo);
                details.setCountry(newCountryInfo);
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<SteamApp> findBookmarkedSteamApps() {
        return serverDetailsRepository.findBookmarkedSteamApps();
    }

    @Override
    public void saveServerList(Collection<ServerDetails> servers) {
        serverDetailsRepository.saveAll(servers);
        serverDetailsRepository.flush();
    }

    @Override
    public CompletableFuture<Integer> findServerListByApp(List<ServerDetails> servers, SteamApp app, WorkProgressCallback<ServerDetails> callback) {
        try {
            if (servers == null)
                throw new IllegalArgumentException("Server list cannot be null");
            if (app == null || app.getId() <= 0)
                throw new IllegalArgumentException("Steam app is either invalid or not specified (null)");

            //Fetch server list from repository
            List<ServerDetails> serverEntities = serverDetailsRepository.findBySteamApp(app);

            log.debug("findServerListByApp() :: Found a total of {} server detail entries from the repository", serverEntities.size());

            int added = 0;

            if (serverEntities.size() > 0) {
                servers.addAll(serverEntities);
                added = serverEntities.size();
                log.debug("findServerListByApp() :: Added {} entities from the repository to the existing cache (New list size: {})", added, servers.size());
            }

            return CompletableFuture.completedFuture(added);
        } catch (Throwable e) {
            log.error("fetchServerList() :: Error thrown while retriving server info list", e);
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    public long fetchNewServerEntries(SteamApp app, WorkProgressCallback<ServerDetails> callback) {

        if (app == null || app.getId() <= 0)
            throw new IllegalArgumentException("Invalid steam app specified");

        log.debug("fetchNewServerEntries() :: Fetching server list for app '{}'", app);

        //Retrieve existing server entries from the repository
        List<ServerDetails> servers = serverDetailsRepository.findBySteamApp(app);

        log.debug("fetchNewServerEntries() :: Found {} server entries from the repository", servers.size());

        log.debug("fetchNewServerEntries() :: Fetching new server addresses from the web service (Instance ID: {})", masterServerQueryClient.hashCode());

        //Mthod #1: Try to retrieve server list from web service
        int added;
        try {
            added = updateServerEntrieFromWebApi(app, servers, callback);
        } catch (Exception e) {
            log.debug("An error occured while trying to retrieve server list from the web service. Falling back to master server method ", e);
            //Method #2: If web service query fails, fallback to the legacy master query
            added = updateServerEntriesFromMaster(app, servers, callback);
        }

        log.debug("updateNewServerEntries() :: Added total of {} entries to the list", added);

        //Save to repository
        if (!servers.isEmpty()) {
            log.debug("fetchNewServerEntries() :: Saving new entries to repo");
            serverDetailsRepository.saveAll(servers);
            log.debug("fetchNewServerEntries() :: Saved {} new server entries to the repository", servers.size());
        }

        return servers.size();
    }

    @Override
    public boolean isBookmarked(ServerDetails server) {
        return serverDetailsRepository.isBookmarked(server);
    }

    @Override
    public void updateBookmarkFlag(ServerDetails server, boolean value) {
        if (server == null)
            return;
        server.setBookmarked(value);
        serverDetailsRepository.saveAndFlush(server);
    }

    @Override
    public List<ServerDetails> findBookmarkedServers() {
        return serverDetailsRepository.findBookmarkedServers();
    }

    @Override
    public int updateServerEntrieFromWebApi(SteamApp app, List<ServerDetails> servers, WorkProgressCallback<ServerDetails> callback) {
        AtomicInteger added = new AtomicInteger();

        log.debug("updateServerEntrieFromWebApi() :: Updating server entries from Web API (Total existing: {})", servers.size());
        steamQueryService.findGameServers(filter.appId(app.getId()), 30000, server -> {
            //Multiple threads maybe accessing this callback at the same time, need to synchronize
            synchronized (servers) {
                Optional<ServerDetails> serverInfo = servers.stream().filter(server::equals).findFirst();

                //If the server entry exists, update. Otherwise, add the new entry
                if (serverInfo.isPresent()) {
                    ServerDetails details = serverInfo.get();
                    details.setName(server.getName());
                    details.setGameDirectory(server.getGameDirectory());
                    details.setVersion(server.getVersion());
                    details.setPlayerCount(server.getPlayerCount());
                    details.setMaxPlayerCount(server.getMaxPlayerCount());
                    details.setMapName(server.getMapName());
                    details.setSecure(server.isSecure());
                    details.setDedicated(server.isDedicated());
                    details.setOperatingSystem(server.getOperatingSystem());
                    details.setSteamId(server.getSteamId());
                } else {
                    //Set steam app
                    server.setSteamApp(app);
                    //Add new entry
                    servers.add(server);
                    added.incrementAndGet();
                }

                invokeIfPresent(callback, server, null);
            }
        }).join();

        return added.get();
    }

    @Override
    public int updateServerEntriesFromMaster(SteamApp app, List<ServerDetails> servers, WorkProgressCallback<ServerDetails> callback) {
        AtomicInteger added = new AtomicInteger();
        log.debug("updateServerEntriesFromMaster() :: Updating server entries from Master");
        masterServerQueryClient.getServerList(MasterServerType.SOURCE, MasterServerRegion.REGION_ALL, filter.appId(app.getId()), (serverAddress, senderAddress, ex) -> {
            if (ex != null) {
                invokeIfPresent(callback, null, ex);
                return;
            }
            if (servers.stream().noneMatch(server -> server.getAddress().equals(serverAddress))) {
                //Create new instance
                ServerDetails sourceServerDetails = new ServerDetails(serverAddress);
                sourceServerDetails.setSteamApp(app);
                servers.add(sourceServerDetails);
                added.incrementAndGet();
                invokeIfPresent(callback, sourceServerDetails, null);
            } else {
                invokeIfPresent(callback, null, new Exception("Address is already on the list: " + serverAddress));
            }
        }).join();

        return 0;
    }

    @Override
    public long getTotalServerEntries(SteamApp app) {
        return serverDetailsRepository.countByApp(app);
    }

    //TODO: Move to util class
    private void copySourceServerDetails(ServerDetails target, SourceServer source) {
        target.setName(source.getName());
        target.setServerTags(source.getServerTags());
        target.setPlayerCount(source.getNumOfPlayers());
        target.setMaxPlayerCount(source.getMaxPlayers());
        target.setGameDirectory(source.getGameDirectory());
        target.setDescription(source.getGameDescription());
        target.setGameId(source.getGameId());
        target.setMapName(source.getMapName());
        target.setGameId(source.getGameId());

        //target.setAppId((int) source.getAppId());
        target.setOperatingSystem(OperatingSystem.valueOf(source.getOperatingSystem()));
        target.setVersion(source.getGameVersion());
        target.setStatus(ServerStatus.ACTIVE);
    }

    @Autowired
    public void setSourceServerQueryClient(SourceQueryClient sourceServerQueryClient) {
        this.sourceServerQueryClient = sourceServerQueryClient;
    }

    @Autowired
    public void setGeoIpService(GeoIpService geoIpService) {
        this.geoIpService = geoIpService;
    }

    @Autowired
    public void setCountryRepository(CountryRepository countryRepository) {
        this.countryRepository = countryRepository;
    }

    @Autowired
    public void setMasterServerQueryClient(MasterServerQueryClient masterServerQueryClient) {
        this.masterServerQueryClient = masterServerQueryClient;
    }

    @Autowired
    public void setServerDetailsRepository(ServerDetailsRepository serverDetailsRepository) {
        this.serverDetailsRepository = serverDetailsRepository;
    }

    @Autowired
    public void setSteamQueryService(SteamService steamQueryService) {
        this.steamQueryService = steamQueryService;
    }

    @Autowired
    public void setEntityMapper(EntityMapper entityMapper) {
        this.entityMapper = entityMapper;
    }
}
