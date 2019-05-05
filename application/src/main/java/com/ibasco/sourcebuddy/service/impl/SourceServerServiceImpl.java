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
import com.ibasco.sourcebuddy.util.WorkProgressCallback;
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
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
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
    public CompletableFuture<ServerDetails> updateAllDetails(ServerDetails server) {
        if (server == null)
            return CompletableFuture.failedFuture(new IllegalArgumentException("Server details cannot be null"));
        return updateServerDetails(server)
                .thenCompose(details -> {
                    if (ServerStatus.ACTIVE.equals(server.getStatus()) && server.getPlayerCount() > 0)
                        return updatePlayerDetails(server).exceptionally(t -> server);
                    return CompletableFuture.completedStage(server);
                }).thenCompose(details -> {
                    if (ServerStatus.ACTIVE.equals(server.getStatus()))
                        return updateServerRules(server).exceptionally(t -> server);
                    return CompletableFuture.completedStage(server);
                });
    }

    @Override
    public void updateAllDetails(List<ServerDetails> servers, WorkProgressCallback<ServerDetails> callback) {
        runBatchAsync(servers, this::updateAllDetails, callback);
    }

    @Override
    public CompletableFuture<ServerDetails> updateServerDetails(ServerDetails target) {
        if (target == null)
            return CompletableFuture.failedFuture(new IllegalArgumentException("Server details is null"));
        return sourceServerQueryClient.getServerInfo(target.getAddress())
                .thenApply(server -> {
                    synchronized (target) {
                        SourceServerServiceImpl.this.copyAndUpdateDetails(target, server);
                        target.setStatus(ServerStatus.ACTIVE);
                    }
                    return target;
                })
                .exceptionally(ex -> {
                    synchronized (target) {
                        if (ex instanceof CompletionException) {
                            if (ex.getCause() instanceof ReadTimeoutException) {
                                target.setStatus(ServerStatus.TIMED_OUT);
                            } else {
                                target.setStatus(ServerStatus.onError(ex));
                                throw (CompletionException) ex;
                            }
                        } else {
                            target.setStatus(ServerStatus.onError(ex));
                            throw new CompletionException(ex);
                        }
                    }
                    return target;
                });
    }

    @Override
    public CompletableFuture<ServerDetails> updatePlayerDetails(ServerDetails server) {
        if (server == null)
            return CompletableFuture.failedFuture(new IllegalArgumentException("Server details is null"));
        return sourceServerQueryClient.getPlayers(server.getAddress())
                .thenApply(entityMapper::map)
                .thenApply(FXCollections::observableList)
                .thenApply(playerList -> {
                    synchronized (server) {
                        server.setPlayers(playerList);
                    }
                    return server;
                })
                .handle(this::handleExceptionEx);
    }

    @Override
    public CompletableFuture<ServerDetails> updateServerRules(ServerDetails server) {
        if (server == null)
            return CompletableFuture.failedFuture(new IllegalArgumentException("Server details is null"));
        return sourceServerQueryClient.getServerRules(server.getAddress())
                .thenApply(FXCollections::observableMap)
                .thenApply(rulesMap -> {
                    synchronized (server) {
                        server.setRules(rulesMap);
                    }
                    return server;
                })
                .handle(this::handleExceptionEx);
    }

    private <U> U handleExceptionEx(U item, Throwable ex) {
        if (ex != null) {
            if (ex instanceof CompletionException) {
                if (!(ex.getCause() instanceof ReadTimeoutException)) {
                    throw (CompletionException) ex;
                }
            } else {
                log.info("Unknown", ex);
                throw new CompletionException(ex);
            }
        }
        return item;
    }

    @Override
    public void updateServerDetails(List<ServerDetails> servers, WorkProgressCallback<ServerDetails> callback) {
        runBatchAsync(servers, this::updateServerDetails, callback);
    }

    @Override
    public void updatePlayerDetails(List<ServerDetails> servers, WorkProgressCallback<ServerDetails> callback) {
        runBatchAsync(servers, this::updatePlayerDetails, callback);
    }

    @Override
    public void updateServerRules(List<ServerDetails> servers, WorkProgressCallback<ServerDetails> callback) {
        runBatchAsync(servers, this::updateServerRules, callback);
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
    public int findServerListByApp(Collection<ServerDetails> servers, SteamApp app, WorkProgressCallback<ServerDetails> callback) {
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

            return added;
        } catch (Throwable e) {
            log.error("fetchServerList() :: Error thrown while retriving server info list", e);
            throw e;
        }
    }

    @Override
    public int fetchNewServerEntries(SteamApp app, WorkProgressCallback<ServerDetails> callback) {

        if (app == null || app.getId() <= 0)
            throw new IllegalArgumentException("Invalid steam app specified");

        log.debug("fetchNewServerEntries() :: Fetching server list for app '{}'", app);

        //Retrieve existing server entries from the repository
        Set<ServerDetails> servers = new HashSet<>(serverDetailsRepository.findBySteamApp(app));

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
    public int updateServerEntrieFromWebApi(SteamApp app, Collection<ServerDetails> servers, WorkProgressCallback<ServerDetails> callback) {
        AtomicInteger added = new AtomicInteger();

        final Object mutext = new Object();

        log.debug("updateServerEntrieFromWebApi() :: Updating server entries from Web API (Total existing: {})", servers.size());
        steamQueryService.findGameServers(filter.appId(app.getId()), 30000, server -> {
            //Multiple threads maybe accessing this callback at the same time, need to synchronize
            synchronized (mutext) {
                Optional<ServerDetails> serverInfo = servers.stream().filter(server::equals).findFirst();
                //If the server entry exists, update. Otherwise, add the new entry
                if (serverInfo.isPresent()) {
                    ServerDetails.copy(server, serverInfo.get());
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
    public int updateServerEntriesFromMaster(SteamApp app, Collection<ServerDetails> servers, WorkProgressCallback<ServerDetails> callback) {
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

    private <T> void runBatchAsync(List<T> items, Function<T, CompletableFuture<T>> func, WorkProgressCallback<T> callback) {
        List<CompletableFuture<Void>> futureList = new ArrayList<>();
        final AtomicReference<Throwable> cancel = new AtomicReference<>();
        try {
            log.info("runBatchAsync() :: Starting batch processing for {} items", items.size());
            for (T item : items) {
                if (cancel.get() != null) {
                    log.info("runBatchAsync() :: Batch process has been interrupted");
                    throw new CancellationException(cancel.get().getMessage());
                }
                CompletableFuture<Void> future = func.apply(item).handle((a, b) -> handleBatchEx(a, b, callback, cancel));
                futureList.add(future);
            }
        } finally {
            List<CompletableFuture<?>> notDone = futureList.stream().filter(f -> !f.isDone()).peek(f -> {
                if (cancel.get() != null) {
                    f.cancel(true);
                }
            }).collect(Collectors.toList());
            log.info("runBatchAsync() :: Waiting for {} remaining future tasks to complete", notDone.size());
            CompletableFuture.allOf(futureList.toArray(new CompletableFuture[0])).join();
            log.info("runBatchAsync() :: Done");
        }
    }

    private <U, T> U handleBatchEx(T item, Throwable ex, WorkProgressCallback<T> callback, AtomicReference<Throwable> cancel) {
        if (ex != null) {
            //Propagate interrupted exceptions, handle timeout exceptions
            if (ex instanceof CompletionException) {
                Throwable cause = ex.getCause();
                //set cancel flag on interruption
                if (cause instanceof InterruptedException) {
                    cancel.set(ex);
                    return null;
                }
                //do not propagate read timeout exceptions, pass it to callback instead
                else if (cause instanceof ReadTimeoutException) {
                    GuiHelper.invokeIfPresent(callback, item, ex);
                } else {
                    GuiHelper.invokeIfPresent(callback, item, ex);
                    throw (CompletionException) ex;
                }
            } else {
                log.debug("Not a completion exception", ex);
                GuiHelper.invokeIfPresent(callback, item, ex);
                throw new CompletionException(ex);
            }
        } else {
            GuiHelper.invokeIfPresent(callback, item, ex);
        }
        return null;
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
