package com.ibasco.sourcebuddy.service.impl;

import com.ibasco.sourcebuddy.domain.ManagedServer;
import com.ibasco.sourcebuddy.domain.ServerDetails;
import com.ibasco.sourcebuddy.repository.ManagedServerRepository;
import com.ibasco.sourcebuddy.service.SourceServerManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class SourceServerManagerImpl implements SourceServerManager {

    private ManagedServerRepository managedServerRepository;

    @Override
    public void addManagedServer(ServerDetails server) {
        ManagedServer managedServer = new ManagedServer();
        managedServer.setServerDetails(server);
        managedServerRepository.save(managedServer);
    }

    @Override
    public void removeManagedServer(ServerDetails server) {
        Optional<ManagedServer> managedServer = managedServerRepository.findByServer(server);
        if (managedServer.isEmpty())
            return;
        managedServerRepository.delete(managedServer.get());
    }

    @Override
    public boolean isManaged(ServerDetails server) {
        return managedServerRepository.findByServer(server).isPresent();
    }

    @Autowired
    public void setManagedServerRepository(ManagedServerRepository managedServerRepository) {
        this.managedServerRepository = managedServerRepository;
    }
}
