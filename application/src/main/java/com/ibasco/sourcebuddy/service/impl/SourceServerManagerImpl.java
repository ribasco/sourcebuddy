package com.ibasco.sourcebuddy.service.impl;

import com.ibasco.sourcebuddy.components.rcon.*;
import com.ibasco.sourcebuddy.components.rcon.parsers.ServerStatusParserFactory;
import com.ibasco.sourcebuddy.components.rcon.parsers.sourcemod.*;
import com.ibasco.sourcebuddy.domain.ManagedServer;
import com.ibasco.sourcebuddy.domain.ServerDetails;
import com.ibasco.sourcebuddy.domain.SteamApp;
import com.ibasco.sourcebuddy.model.AppModel;
import com.ibasco.sourcebuddy.repository.ConfigProfileRepository;
import com.ibasco.sourcebuddy.repository.ManagedServerRepository;
import com.ibasco.sourcebuddy.repository.ServerDetailsRepository;
import com.ibasco.sourcebuddy.service.RconService;
import com.ibasco.sourcebuddy.service.SourceServerManager;
import com.ibasco.sourcebuddy.util.Check;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@Transactional
public class SourceServerManagerImpl implements SourceServerManager {

    private static final Logger log = LoggerFactory.getLogger(SourceServerManagerImpl.class);

    private ManagedServerRepository managedServerRepository;

    private ServerDetailsRepository serverDetailsRepository;

    private ConfigProfileRepository configProfileRepository;

    private AppModel appModel;

    private RconService rconService;

    private ServerStatusParserFactory rconStatusParserFactory;

    @Override
    public ManagedServer addServer(ServerDetails server) {
        if (isManaged(server)) {
            log.debug("addServer() :: Cannot add server '{}'. Server is already in the managed state", server.getAddress());
            return null;
        }
        ManagedServer managedServer = new ManagedServer();
        managedServer.setProfile(appModel.getActiveProfile());
        managedServer.setServerDetails(server);
        managedServer = managedServerRepository.saveAndFlush(managedServer);
        refreshActiveProfile();
        return managedServer;
    }

    @Override
    public void removeServer(ServerDetails server) {
        managedServerRepository.findByServer(server).ifPresent(this::removeServer);
    }

    @Override
    public void removeServer(ManagedServer server) {
        Check.requireNonNull(server, "Server cannot be null");
        managedServerRepository.delete(server);
        refreshActiveProfile();
    }

    @Override
    public boolean isManaged(ServerDetails server) {
        server = serverDetailsRepository.refresh(server);
        return managedServerRepository.findByServer(server).isPresent();
    }

    @Override
    public ManagedServer save(ManagedServer server) {
        return managedServerRepository.save(server);
    }

    @Override
    public void deleteAllServers() {
        managedServerRepository.deleteAll();
        refreshActiveProfile();
    }

    @Override
    public List<ManagedServer> findManagedServer() {
        return managedServerRepository.findAll();
    }

    @Override
    public List<ManagedServer> findManagedServer(SteamApp app) {
        return managedServerRepository.findByApp(app);
    }

    @Override
    public ManagedServer findManagedServer(ServerDetails server) {
        return managedServerRepository.findByServer(server).orElse(null);
    }

    @Override
    public CompletableFuture<SourceServerStatus> getServerStatus(ManagedServer server) {
        return rconService.tryExecuteAndParse(server, "status", rconStatusParserFactory.create(server));
    }

    @Override
    public CompletableFuture<List<SourceModPlugin>> getPlugins(ManagedServer server) {
        return rconService.tryExecuteAndParse(server, "sm plugins list", new SourceModPluginListParser());
    }

    @Override
    public CompletableFuture<Void> reloadPlugins(ManagedServer server) {
        return rconService.tryExecute(server, "sm plugins refresh").thenAccept(s -> {
        });
    }

    @Override
    public CompletableFuture<SourceModPlugin> updatePluginInfo(SourceModPlugin plugin) {
        if (plugin.getServer() == null)
            throw new IllegalArgumentException("Managed server not specified");
        return rconService.tryExecuteAndParse(plugin.getServer(), String.format("sm plugins info %d", plugin.getIndex()), new SourceModPluginInfoParser(plugin));
    }

    @Override
    public CompletableFuture<Void> reloadPlugin(SourceModPlugin plugin) {
        return null;
    }

    @Override
    public CompletableFuture<Void> unloadAllPlugins(ManagedServer server) {
        return null;
    }

    @Override
    public CompletableFuture<Void> unloadPlugin(SourceModPlugin plugin) {
        return null;
    }

    @Override
    public CompletableFuture<List<SourceModCvar>> getPluginCvars(SourceModPlugin plugin) {
        return rconService.tryExecuteAndParse(plugin.getServer(), String.format("sm cvars %d", plugin.getIndex()), new SourceModPluginCvarParser());
    }

    @Override
    public CompletableFuture<List<SourceModExtension>> getExtensions(ManagedServer server) {
        return rconService.tryExecuteAndParse(server, "sm exts list", new SourceModExtListParser());
    }

    @Override
    public CompletableFuture<List<SourceModCommand>> getCommands(SourceModPlugin plugin) {
        return rconService.tryExecuteAndParse(plugin.getServer(), String.format("sm cmds %d", plugin.getIndex()), new SourceModCommandListParser());
    }

    @Override
    public CompletableFuture<Void> protectConvar(ManagedServer server, String cvar) {
        return rconService.tryExecuteAndParse(server, String.format("sm cvar protect %s", cvar), new SourceModCommandListParser()).thenAccept(SourceServerManagerImpl::acceptEmpty);
    }

    @Override
    public CompletableFuture<SourceModCvarChangeResult> updateConvar(ManagedServer server, String cvar, Object value) {
        return rconService.tryExecuteAndParse(server, String.format("sm cvar %s %s", cvar, value != null ? value.toString() : ""), new SourceModCvarChangeParser());
    }

    private void refreshActiveProfile() {
        //configProfileRepository.refresh(appModel.getActiveProfile());
        appModel.setActiveProfile(configProfileRepository.refresh(appModel.getActiveProfile()));
    }

    private static <T> void acceptEmpty(T plugin1) {

    }

    @Autowired
    public void setManagedServerRepository(ManagedServerRepository managedServerRepository) {
        this.managedServerRepository = managedServerRepository;
    }

    @Autowired
    public void setAppModel(AppModel appModel) {
        this.appModel = appModel;
    }

    @Autowired
    public void setConfigProfileRepository(ConfigProfileRepository configProfileRepository) {
        this.configProfileRepository = configProfileRepository;
    }

    @Autowired
    public void setServerDetailsRepository(ServerDetailsRepository serverDetailsRepository) {
        this.serverDetailsRepository = serverDetailsRepository;
    }

    @Autowired
    public void setRconService(RconService rconService) {
        this.rconService = rconService;
    }

    @Autowired
    public void setRconStatusParserFactory(ServerStatusParserFactory rconStatusParserFactory) {
        this.rconStatusParserFactory = rconStatusParserFactory;
    }
}
