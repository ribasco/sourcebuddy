package com.ibasco.sourcebuddy.service.impl;

import com.ibasco.sourcebuddy.domain.ManagedServer;
import com.ibasco.sourcebuddy.domain.ServerDetails;
import com.ibasco.sourcebuddy.domain.SteamApp;
import com.ibasco.sourcebuddy.model.ServerDetailsModel;
import com.ibasco.sourcebuddy.repository.ConfigProfileRepository;
import com.ibasco.sourcebuddy.repository.ManagedServerRepository;
import com.ibasco.sourcebuddy.service.ServerManager;
import com.ibasco.sourcebuddy.util.Check;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ServerManagerImpl implements ServerManager {

    private static final Logger log = LoggerFactory.getLogger(ServerManagerImpl.class);

    private ManagedServerRepository managedServerRepository;

    private ConfigProfileRepository configProfileRepository;

    private ServerDetailsModel serverDetailsModel;

    @Override
    public ManagedServer addServer(ServerDetails server) {
        if (isManaged(server)) {
            log.debug("addServer() :: Cannot add server '{}'. Server is already in the managed state", server.getAddress());
            return null;
        }
        ManagedServer managedServer = new ManagedServer();
        managedServer.setProfile(serverDetailsModel.getActiveProfile());
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
        return managedServerRepository.findByServer(server).isPresent();
    }

    @Override
    public void clear() {
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

    private void refreshActiveProfile() {
        configProfileRepository.refresh(serverDetailsModel.getActiveProfile());
        //serverDetailsModel.setActiveProfile(configProfileRepository.refresh(serverDetailsModel.getActiveProfile()));
    }

    @Autowired
    public void setManagedServerRepository(ManagedServerRepository managedServerRepository) {
        this.managedServerRepository = managedServerRepository;
    }

    @Autowired
    public void setServerDetailsModel(ServerDetailsModel serverDetailsModel) {
        this.serverDetailsModel = serverDetailsModel;
    }

    @Autowired
    public void setConfigProfileRepository(ConfigProfileRepository configProfileRepository) {
        this.configProfileRepository = configProfileRepository;
    }
}
