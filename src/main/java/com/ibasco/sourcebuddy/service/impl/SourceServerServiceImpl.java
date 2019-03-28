package com.ibasco.sourcebuddy.service.impl;

import com.ibasco.agql.core.exceptions.ReadTimeoutException;
import com.ibasco.agql.core.utils.ServerFilter;
import com.ibasco.agql.protocols.valve.source.query.client.SourceQueryClient;
import com.ibasco.agql.protocols.valve.source.query.pojos.SourcePlayer;
import com.ibasco.agql.protocols.valve.source.query.pojos.SourceServer;
import com.ibasco.agql.protocols.valve.steam.master.client.MasterServerQueryClient;
import com.ibasco.agql.protocols.valve.steam.master.enums.MasterServerRegion;
import com.ibasco.agql.protocols.valve.steam.master.enums.MasterServerType;
import com.ibasco.sourcebuddy.domain.PlayerInfo;
import com.ibasco.sourcebuddy.domain.ServerDetails;
import com.ibasco.sourcebuddy.domain.SteamApp;
import com.ibasco.sourcebuddy.enums.OperatingSystem;
import com.ibasco.sourcebuddy.enums.ServerStatus;
import com.ibasco.sourcebuddy.repository.CountryRepository;
import com.ibasco.sourcebuddy.repository.ServerDetailsRepository;
import com.ibasco.sourcebuddy.service.GeoIpService;
import com.ibasco.sourcebuddy.service.SourceServerService;
import com.ibasco.sourcebuddy.service.SteamQueryService;
import static com.ibasco.sourcebuddy.util.GuiUtil.invokeIfPresent;
import static com.ibasco.sourcebuddy.util.GuiUtil.mergeList;
import com.ibasco.sourcebuddy.util.ThreadUtil;
import com.ibasco.sourcebuddy.util.WorkProgressCallback;
import com.maxmind.geoip2.record.Country;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.*;
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

    private SteamQueryService steamQueryService;

    private Set<com.ibasco.sourcebuddy.domain.Country> countrySet;

    @PostConstruct
    private void init() {
        countrySet = new HashSet<>(countryRepository.findAll());
        log.debug("init() :: Cached a total of {} countries", countrySet.size());
    }

    @Override
    public void updateServerDetails(List<ServerDetails> servers, WorkProgressCallback<ServerDetails> callback) throws InterruptedException {
        log.debug("updateServerDetails() :: Running server details update for {} servers", servers.size());
        CountDownLatch latch = new CountDownLatch(servers.size());
        servers.parallelStream().forEach(target -> {
            sourceServerQueryClient.getServerInfo(target.getAddress())
                    .whenComplete((server, ex) -> {
                        if (ex == null) {
                            copySourceServerDetails(target, server);
                        } else {
                            if (ex.getCause() instanceof ReadTimeoutException) {
                                target.setStatus(ServerStatus.TIMED_OUT);
                            } else {
                                target.setStatus(ServerStatus.ERRORED);
                            }
                        }
                        invokeIfPresent(callback, target, ex);
                        latch.countDown();
                    });
            ThreadUtil.sleepUninterrupted(5);
        });

        //Update country details
        log.debug("updateServerEntries() :: Updating country details for new {} server entries", servers.size());
        servers.forEach(this::updateCountryDetails);

        log.debug("updateServerDetails() :: Waiting for completion of server details update");
        latch.await();
    }

    @Override
    public void updatePlayerDetails(List<ServerDetails> servers, WorkProgressCallback<ServerDetails> callback) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(servers.size());
        log.debug("updatePlayerDetails() :: Running player details update for {} active and non-empty servers", servers.size());
        servers.parallelStream().forEach(target -> {
            sourceServerQueryClient.getPlayers(target.getAddress()).whenComplete((playerList, ex) -> {
                if (ex == null)
                    target.setPlayers(toSourcePlayerInfoList(playerList));
                invokeIfPresent(callback, target, ex);
                latch.countDown();
            });
            ThreadUtil.sleepUninterrupted(5);
        });
        log.debug("updatePlayerDetails() :: Waiting for completion of server players update");
        latch.await();
    }

    @Override
    public void updateServerRules(List<ServerDetails> servers, WorkProgressCallback<ServerDetails> callback) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(servers.size());
        log.debug("updateServerRules() :: Running server rules update for {} active servers", servers.size());
        servers.parallelStream().forEach(info -> {
            sourceServerQueryClient.getServerRules(info.getAddress()).whenComplete((rulesMap, ex) -> {
                if (ex == null)
                    info.setRules(FXCollections.observableMap(rulesMap));
                invokeIfPresent(callback, info, ex);
                latch.countDown();
            });
            ThreadUtil.sleepUninterrupted(5);
        });
        log.debug("updateServerRules() :: Waiting for completion of server rulse update");
        latch.await();
    }

    @Override
    public void updateCountryDetails(ServerDetails details) {
        if (details.getCountry() != null)
            return;
        //Add country information
        Country countryInfo = geoIpService.findCountry(details.getAddress());
        if (countryInfo != null && countryInfo.getIsoCode() != null) {
            countrySet.stream()
                    .filter(c -> c.getCountryCode().equals(countryInfo.getIsoCode()))
                    .findFirst()
                    .ifPresentOrElse(details::setCountry, () -> {
                        com.ibasco.sourcebuddy.domain.Country country = new com.ibasco.sourcebuddy.domain.Country();
                        country.setCountryCode(countryInfo.getIsoCode());
                        country.setCountryName(countryInfo.getName());
                        country = countryRepository.save(country);
                        details.setCountry(country);
                    });
        }
    }

    @Override
    public ServerDetails updateBookmarkFlag(ServerDetails server, boolean value) {
        server.setBookmarked(value);
        return serverDetailsRepository.saveAndFlush(server);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServerDetails> findBookmarks(SteamApp steamApp) {
        return serverDetailsRepository.findBookmarksByApp(steamApp);
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
    @Transactional(readOnly = true)
    public int findServerListByApp(List<ServerDetails> servers, SteamApp app, boolean update, WorkProgressCallback<ServerDetails> callback) {
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
                added = mergeList(serverEntities, servers, callback);
                log.debug("findServerListByApp() :: Merged {} entities from the repository to the existing cach (New list size: {})", added, servers.size());
            }

            return added;
        } catch (Throwable e) {
            log.error("fetchServerList() :: Error thrown while retriving server info list", e);
            throw e;
        }
    }

    @Override
    public long updateServerEntries(SteamApp app, WorkProgressCallback<ServerDetails> callback) {

        if (app == null || app.getId() <= 0)
            throw new IllegalArgumentException("Invalid steam app specified");

        log.debug("updateServerEntries() :: Fetching server list for app '{}'", app);

        //Retrieve existing server entries from the repository
        List<ServerDetails> servers = serverDetailsRepository.findBySteamApp(app);

        log.debug("updateServerEntries() :: Found {} server entries from the repository", servers.size());

        log.debug("updateServerEntries() :: Fetching new server addresses from the web service (Instance ID: {})", masterServerQueryClient.hashCode());

        //Mthod #1: Fetch from web api
        int added;
        try {
            added = updateServerEntrieFromWebApi(app, servers, callback);
        } catch (Exception e) {
            log.debug("An error occured while trying to retrieve server list from the web service. Falling back to master server method ", e);
            //Method #2: Fetch from master server
            added = updateServerEntriesFromMaster(app, servers, callback);
        }

        log.debug("updateServerEntries() :: Added total of {} entries to the list", added);

        //Save to repository
        if (!servers.isEmpty()) {
            log.debug("updateServerEntries() :: Saving new entries to repo");
            serverDetailsRepository.saveAll(servers);
            log.debug("updateServerEntries() :: Saved {} new server entries to the repository", servers.size());
        }

        return servers.size();
    }

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

    private ObservableList<PlayerInfo> toSourcePlayerInfoList(List<SourcePlayer> sourcePlayers) {
        return sourcePlayers.stream().map(PlayerInfo::new).collect(Collectors.toCollection(FXCollections::observableArrayList));
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
    public void setSteamQueryService(SteamQueryService steamQueryService) {
        this.steamQueryService = steamQueryService;
    }
}
