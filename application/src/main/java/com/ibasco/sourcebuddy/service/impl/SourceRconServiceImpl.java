package com.ibasco.sourcebuddy.service.impl;

import com.ibasco.agql.protocols.valve.source.query.client.SourceRconClient;
import com.ibasco.agql.protocols.valve.source.query.exceptions.RconNotYetAuthException;
import com.ibasco.sourcebuddy.domain.RconStatus;
import com.ibasco.sourcebuddy.exceptions.NotAuthenticatedException;
import com.ibasco.sourcebuddy.service.RconService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

@Service
public class SourceRconServiceImpl implements RconService {

    private SourceRconClient sourceRconClient;

    @Override
    public CompletableFuture<RconStatus> authenticate(InetSocketAddress address, String password) {
        return sourceRconClient.authenticate(address, password).thenApply(s -> new RconStatus(s.isAuthenticated(), s.getReason()));
    }

    @Override
    public boolean isAuthenticated(InetSocketAddress address) {
        return sourceRconClient.isAuthenticated(address);
    }

    @Override
    public CompletableFuture<String> execute(InetSocketAddress address, String command) throws NotAuthenticatedException {
        try {
            return sourceRconClient.execute(address, command);
        } catch (RconNotYetAuthException e) {
            throw new NotAuthenticatedException(e);
        }
    }

    @Autowired
    public void setSourceRconClient(SourceRconClient sourceRconClient) {
        this.sourceRconClient = sourceRconClient;
    }
}
