package com.ibasco.sourcebuddy.service.impl;

import com.ibasco.agql.core.exceptions.ReadTimeoutException;
import com.ibasco.agql.core.utils.ServerFilter;
import com.ibasco.agql.protocols.valve.source.query.client.SourceQueryClient;
import com.ibasco.agql.protocols.valve.source.query.pojos.SourceServer;
import com.ibasco.agql.protocols.valve.steam.master.client.MasterServerQueryClient;
import com.ibasco.agql.protocols.valve.steam.master.enums.MasterServerRegion;
import com.ibasco.agql.protocols.valve.steam.master.enums.MasterServerType;
import com.ibasco.sourcebuddy.components.EntityMapper;
import com.ibasco.sourcebuddy.components.GuiHelper;
import com.ibasco.sourcebuddy.constants.Qualifiers;
import com.ibasco.sourcebuddy.domain.Country;
import com.ibasco.sourcebuddy.domain.ServerDetails;
import com.ibasco.sourcebuddy.domain.SteamApp;
import com.ibasco.sourcebuddy.enums.ServerStatus;
import com.ibasco.sourcebuddy.repository.CountryRepository;
import com.ibasco.sourcebuddy.repository.ServerDetailsRepository;
import com.ibasco.sourcebuddy.service.GeoIpService;
import com.ibasco.sourcebuddy.service.SourceServerService;
import com.ibasco.sourcebuddy.service.SteamService;
import com.ibasco.sourcebuddy.util.ServerDetailsFilter;
import com.ibasco.sourcebuddy.util.ThreadUtil;
import com.ibasco.sourcebuddy.util.WorkProgressCallback;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

@Service
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
    @Async(Qualifiers.TASK_EXECUTOR_SERVICE)
    public CompletableFuture<Void> updateAllServerDetails(List<ServerDetails> servers, WorkProgressCallback<ServerDetails> callback) {
        if (servers.isEmpty()) {
            log.warn("No available servers to update. Server list is empty");
            return CompletableFuture.completedFuture(null);
        }

        List<CompletableFuture<?>> futureList = new ArrayList<>();

        log.info("updateAllServerDetails()  :: Starting batch server details update (Size: {})", servers.size());
        CompletableFuture<?> future = updateServerDetails(servers, callback);
        futureList.add(future);
        future.join();

        //Update player details (active and non-empty servers only)
        List<ServerDetails> filteredList = servers.stream().filter(ServerDetailsFilter::byActiveServers).filter(ServerDetailsFilter::byNonEmptyServers).collect(Collectors.toList());
        log.info("updateAllServerDetails() :: Starting batch player details update (Size: {})", filteredList.size());
        future = updatePlayerDetails(filteredList, callback);
        futureList.add(future);
        future.join();

        //Update server rules (active servers only)
        filteredList = servers.stream().filter(ServerDetailsFilter::byActiveServers).collect(Collectors.toList());
        log.info("updateAllServerDetails() :: Starting batch server rules update (Size: {})", filteredList.size());
        future = updateServerRules(filteredList, callback);
        futureList.add(future);
        future.join();

        //Save to database
        if (!servers.isEmpty()) {
            log.info("updateAllServerDetails() :: Saving {} entries to database", servers.size());
            save(servers);
            log.info("updateAllServerDetails() :: Successfully saved {} entries to database", servers.size());
        }

        return CompletableFuture.allOf(futureList.toArray(new CompletableFuture[0]));
    }

    @Override
    public CompletableFuture<Void> updateServerDetails(ServerDetails target) {
        return sourceServerQueryClient.getServerInfo(target.getAddress())
                .whenComplete((server, ex) -> {
                    if (ex != null) {
                        if (ex.getCause() instanceof ReadTimeoutException) {
                            target.setStatus(ServerStatus.TIMED_OUT);
                        } else {
                            target.setStatus(ServerStatus.onError(ex));
                        }
                        log.debug("Error on server request", ex);
                    } else {
                        copyAndUpdateDetails(target, server);
                        target.setStatus(ServerStatus.ACTIVE);
                    }
                }).thenAccept(s -> {
                });
    }

    @Override
    public CompletableFuture<Void> updatePlayerDetails(ServerDetails target) {
        return sourceServerQueryClient.getPlayers(target.getAddress())
                .thenApply(entityMapper::map)
                .thenApply(FXCollections::observableList)
                .thenAccept(playerList -> {
                    if (!Platform.isFxApplicationThread())
                        Platform.runLater(() -> target.setPlayers(playerList));
                    else
                        target.setPlayers(playerList);
                });
    }

    @Override
    public CompletableFuture<Void> updateServerRules(ServerDetails target) {
        return sourceServerQueryClient.getServerRules(target.getAddress())
                .thenApply(FXCollections::observableMap)
                .thenAccept(rulesMap -> {
                    if (!Platform.isFxApplicationThread())
                        Platform.runLater(() -> target.setRules(rulesMap));
                    else
                        target.setRules(rulesMap);
                });
    }

    @Override
    @Async(Qualifiers.TASK_EXECUTOR_SERVICE)
    public CompletableFuture<Void> updateServerDetails(List<ServerDetails> servers, WorkProgressCallback<ServerDetails> callback) {
        log.info("updateServerDetails() :: Running server details update for {} servers", servers.size());
        List<CompletableFuture<?>> cfList = Collections.synchronizedList(new ArrayList<>());
        servers.parallelStream().forEach(target -> {
            CompletableFuture<?> future = updateServerDetails(target)
                    .handle((BiFunction<Void, Throwable, Void>) (aVoid, ex) -> {
                        GuiHelper.invokeIfPresent(callback, target, ex);
                        return null;
                    });
            cfList.add(future);
            ThreadUtil.sleepUninterrupted(10);
        });
        return CompletableFuture.allOf(cfList.toArray(new CompletableFuture[0]));
    }

    @Override
    @Async(Qualifiers.TASK_EXECUTOR_SERVICE)
    public CompletableFuture<Void> updatePlayerDetails(List<ServerDetails> servers, WorkProgressCallback<ServerDetails> callback) {
        List<CompletableFuture<?>> cfList = Collections.synchronizedList(new ArrayList<>());
        log.info("updatePlayerDetails() :: Running player details update for {} active and non-empty servers", servers.size());
        servers.parallelStream().forEach(target -> {
            CompletableFuture<?> cf = updatePlayerDetails(target).handle((playerList, ex) -> {
                GuiHelper.invokeIfPresent(callback, target, ex);
                return target;
            });
            cfList.add(cf);
            ThreadUtil.sleepUninterrupted(10);
        });
        return CompletableFuture.allOf(cfList.toArray(new CompletableFuture[0]));
    }

    @Override
    @Async(Qualifiers.TASK_EXECUTOR_SERVICE)
    public CompletableFuture<Void> updateServerRules(List<ServerDetails> servers, WorkProgressCallback<ServerDetails> callback) {
        List<CompletableFuture<?>> cfList = Collections.synchronizedList(new ArrayList<>());
        log.info("updateServerRules() :: Running server rules update for {} active servers", servers.size());
        servers.parallelStream().forEach(target -> {
            CompletableFuture<?> cf = updateServerRules(target).handle((rulesMap, ex) -> {
                GuiHelper.invokeIfPresent(callback, target, ex);
                return target;
            });
            cfList.add(cf);
            ThreadUtil.sleepUninterrupted(10);
        });
        return CompletableFuture.allOf(cfList.toArray(new CompletableFuture[0]));
    }

    @Override
    @Async(Qualifiers.TASK_EXECUTOR_SERVICE)
    public CompletableFuture<ServerDetails> findServerDetails(InetSocketAddress address) {
        Optional<ServerDetails> res = serverDetailsRepository.findByAddress(address.getAddress().getHostAddress(), address.getPort());
        return res.map(CompletableFuture::completedFuture)
                .orElseGet(() -> {
                    final ServerDetails details = new ServerDetails(address);
                    return updateServerDetails(details).thenApply(aVoid -> details);
                });
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
    public void save(Collection<ServerDetails> servers) {
        serverDetailsRepository.saveAll(servers);
        serverDetailsRepository.flush();
    }

    @Override
    public void save(ServerDetails server) {
        serverDetailsRepository.saveAndFlush(server);
    }

    @Override
    public boolean exists(ServerDetails serverDetails) {
        return serverDetailsRepository.exists(Example.of(serverDetails));
    }

    @Override
    public boolean exists(InetSocketAddress address) {
        return serverDetailsRepository.findByAddress(address.getAddress().getHostAddress(), address.getPort()).isPresent();
    }

    @Override
    @Async(Qualifiers.STEAM_EXECUTOR_SERVICE)
    public CompletableFuture<Integer> findServerListByApp(List<ServerDetails> servers, SteamApp app, WorkProgressCallback<ServerDetails> callback) {
        try {
            if (servers == null)
                throw new IllegalArgumentException("Server list cannot be null");
            if (app == null || app.getId() <= 0)
                throw new IllegalArgumentException("Steam app is either invalid or not specified (null)");

            log.info("findServerListByApp() :: Checking if server entries are present in the database for app = {}", app);

            //Fetch server list from repository
            List<ServerDetails> serverEntities = serverDetailsRepository.findBySteamApp(app);

            log.info("findServerListByApp() :: Found a total of {} server detail entries from the repository", serverEntities.size());

            int added = 0;

            if (serverEntities.size() > 0) {
                servers.addAll(serverEntities);
                added = serverEntities.size();
                log.info("findServerListByApp() :: Added {} entities from the repository to the existing cache (New list size: {})", added, servers.size());
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

        log.debug("fetchNewServerEntries() :: Added total of {} entries to the list", added);

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
    public List<ServerDetails> findBookmarkedServers(SteamApp app) {
        return serverDetailsRepository.findBookmarksByApp(app);
    }

    @Override
    public int updateServerEntrieFromWebApi(SteamApp app, List<ServerDetails> servers, WorkProgressCallback<ServerDetails> callback) {
        AtomicInteger added = new AtomicInteger();

        final Object mutext = new Object();

        log.debug("updateServerEntrieFromWebApi() :: Updating server entries from Web API (Total existing: {})", servers.size());
        steamQueryService.findGameServers(filter.appId(app.getId()), 30000, server -> {
            //Multiple threads maybe accessing this callback at the same time, need to synchronize
            synchronized (mutext) {
                Optional<ServerDetails> serverInfo = servers.stream().filter(server::equals).findFirst();

                //If the server entry exists, update. Otherwise, add the new entry
                if (serverInfo.isPresent()) {
                    ServerDetails details = serverInfo.get();

                    //BeanUtils.copyProperties(server, details, "id");
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

                GuiHelper.invokeIfPresent(callback, server, null);
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
                GuiHelper.invokeIfPresent(callback, null, ex);
                return;
            }
            if (servers.stream().noneMatch(server -> server.getAddress().equals(serverAddress))) {
                //Create new instance
                ServerDetails sourceServerDetails = new ServerDetails(serverAddress);
                sourceServerDetails.setSteamApp(app);
                servers.add(sourceServerDetails);
                added.incrementAndGet();
                GuiHelper.invokeIfPresent(callback, sourceServerDetails, null);
            } else {
                GuiHelper.invokeIfPresent(callback, null, new Exception("Address is already on the list: " + serverAddress));
            }
        }).join();

        return 0;
    }

    @Override
    public long getTotalServerEntries(SteamApp app) {
        return serverDetailsRepository.countByApp(app);
    }

    /**
     * Copy properties from source to target and update steam app and country information
     *
     * @param target
     *         The target entity
     * @param source
     *         The source entity
     */
    private void copyAndUpdateDetails(ServerDetails target, SourceServer source) {
        entityMapper.copy(target, source);
        if (target.getSteamApp() == null) {
            Optional<SteamApp> res = steamQueryService.findSteamAppById(source.getAppId());
            res.ifPresent(target::setSteamApp);
        }
        updateCountryDetails(target);
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
