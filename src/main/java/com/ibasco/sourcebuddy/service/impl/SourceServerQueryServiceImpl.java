package com.ibasco.sourcebuddy.service.impl;

import com.ibasco.agql.core.exceptions.ReadTimeoutException;
import com.ibasco.agql.protocols.valve.source.query.client.SourceQueryClient;
import com.ibasco.agql.protocols.valve.source.query.pojos.SourcePlayer;
import com.ibasco.agql.protocols.valve.source.query.pojos.SourceServer;
import com.ibasco.agql.protocols.valve.steam.master.MasterServerFilter;
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
import com.ibasco.sourcebuddy.service.SourceServerQueryService;
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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Transactional
public class SourceServerQueryServiceImpl implements SourceServerQueryService {

    private static final Logger log = LoggerFactory.getLogger(SourceServerQueryServiceImpl.class);

    private final MasterServerFilter filter = MasterServerFilter.create().dedicated(true);

    private SourceQueryClient sourceServerQueryClient;

    private GeoIpService geoIpService;

    private CountryRepository countryRepository;

    private MasterServerQueryClient masterServerQueryClient;

    private ServerDetailsRepository serverDetailsRepository;

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
        Country countryInfo = geoIpService.findCountryByAddress(details.getAddress());
        if (countryInfo != null && countryInfo.getIsoCode() != null) {
            //Check if we have an entity saved in the database
            countryRepository.findById(countryInfo.getIsoCode()).ifPresentOrElse(details::setCountry, () -> {
                com.ibasco.sourcebuddy.domain.Country country = new com.ibasco.sourcebuddy.domain.Country();
                country.setCountryCode(countryInfo.getIsoCode());
                country.setCountryName(countryInfo.getName());
                details.setCountry(countryRepository.save(country));
            });
        }
    }

    @Override
    public int populateServerList(List<ServerDetails> servers, SteamApp app, boolean update, WorkProgressCallback<ServerDetails> callback) {
        try {
            if (servers == null)
                throw new IllegalArgumentException("Server list cannot be null");

            log.debug("populateServerList() :: Checking existing cache");

            //check if we have existing entries in the cache
            if (servers.size() > 0 && !update) {
                log.debug("populateServerList() :: Got {} entries from existing cache. Exiting", servers.size());
                return servers.size();
            }

            log.debug("populateServerList() :: No entries in cache");

            final AtomicInteger masterCtr = new AtomicInteger();

            log.debug("populateServerList() :: Checking existing entries in repository");

            if (app == null) {
                log.debug("populateServerList() :: Steam App not specified. Exiting.");
                return servers.size();
            }

            log.debug("populateServerList() :: Fetching steam app details for {} (Id: {})", app.getName(), app.getId());

            //Fetch server list from repository
            List<ServerDetails> serverEntities = serverDetailsRepository.findBySteamApp(app);

            log.debug("populateServerList() :: Got total of {} server entries from the repository", serverEntities.size());

            if (serverEntities.size() > 0) {
                int res = mergeList(serverEntities, servers, callback);
                masterCtr.addAndGet(res);
                log.debug("populateServerList() :: Merged {} entities from the repository to the existing cach (New list size: {})", res, servers.size());
            }

            if ((!update || app.getId() <= 0) && servers.size() > 0) {
                log.debug("populateServerList() :: Either app id is invalid or forcee update flag is not set. Returning entries retrieved from repository (App ID: {}, Update Flag: {}, Total: {})", app, update, masterCtr.get());
                return masterCtr.get();
            }

            log.debug("populateServerList() :: Retrieving fresh list of entries from the master server");

            //no server entries available, time to populate
            final Set<ServerDetails> tmp = new HashSet<>(servers);

            masterServerQueryClient.getServerList(MasterServerType.SOURCE, MasterServerRegion.REGION_ALL, filter.appId(app.getId()), (serverAddress, senderAddress, ex) -> {
                if (ex != null)
                    return;
                //Create new instance
                ServerDetails sourceServerDetails = new ServerDetails(serverAddress.getAddress().getHostAddress(), serverAddress.getPort());

                //Update country information
                updateCountryDetails(sourceServerDetails);
                if (tmp.add(sourceServerDetails)) {
                    masterCtr.incrementAndGet();
                    invokeIfPresent(callback, sourceServerDetails, null);
                }
            }).join();

            log.debug("populateServerList() :: Got a total of {} entries from the master server", tmp.size());

            servers.clear();
            servers.addAll(tmp);

            //Update steamapp
            servers.parallelStream().forEach(server -> server.setSteamApp(app));

            log.debug("Completed server ip refresh. Total: {}", servers.size());
            return masterCtr.get();
        } catch (Throwable e) {
            log.error("Error thrown while retriving server info list", e);
            throw e;
        }
    }

    @Override
    public void updateMasterServerList() {

    }

    //TODO: Move to a util class
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
}
