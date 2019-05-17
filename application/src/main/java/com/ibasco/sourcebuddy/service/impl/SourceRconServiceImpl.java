package com.ibasco.sourcebuddy.service.impl;

import com.ibasco.agql.core.exceptions.ReadTimeoutException;
import com.ibasco.agql.protocols.valve.source.query.client.SourceRconClient;
import com.ibasco.agql.protocols.valve.source.query.exceptions.RconNotYetAuthException;
import com.ibasco.sourcebuddy.domain.ManagedServer;
import com.ibasco.sourcebuddy.domain.RconAuthStatus;
import com.ibasco.sourcebuddy.exceptions.NotAuthenticatedException;
import com.ibasco.sourcebuddy.service.RconService;
import com.ibasco.sourcebuddy.util.Check;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.function.BiFunction;

@Service
public class SourceRconServiceImpl implements RconService {

    private static final Logger log = LoggerFactory.getLogger(SourceRconServiceImpl.class);

    private SourceRconClient sourceRconClient;

    private Map<ManagedServer, RconAuthStatus> authStatus = new HashMap<>();

    @Value("${app.rcon.max-retries}")
    private int maxRetries;

    private ExecutorService rconExecutorService;

    @Override
    public CompletableFuture<RconAuthStatus> authenticate(ManagedServer managedServer) {
        InetSocketAddress address = managedServer.getServerDetails().getAddress();
        return sourceRconClient.authenticate(address, managedServer.getRconPassword())
                .thenApply(s -> new RconAuthStatus(s.isAuthenticated(), s.getReason()))
                .exceptionally(ex -> new RconAuthStatus(false, ex.getMessage(), ex))
                .thenApply(rconAuthStatus -> {
                    RconAuthStatus status = authStatus.computeIfAbsent(managedServer, address1 -> rconAuthStatus);
                    status.setAuthenticated(rconAuthStatus.isAuthenticated());
                    status.setReason(rconAuthStatus.getReason());
                    return status;
                });
    }

    @Override
    public boolean isAuthenticated(ManagedServer managedServer) {
        RconAuthStatus status = authStatus.get(managedServer);
        return status != null && status.isAuthenticated();
    }

    @Override
    public RconAuthStatus getStatus(ManagedServer managedServer) {
        return authStatus.get(managedServer);
    }

    @Override
    public CompletableFuture<String> execute(ManagedServer managedServer, String command) throws NotAuthenticatedException {
        try {
            InetSocketAddress address = managedServer.getServerDetails().getAddress();
            return sourceRconClient.execute(address, command).thenApply(result -> {
                if (result != null && result.toLowerCase().contains("bad password")) {
                    RconAuthStatus status = authStatus.get(managedServer);
                    status.setAuthenticated(false);
                    status.setReason("Bad Password");
                    throw new CompletionException(new NotAuthenticatedException("Re-authentication needed"));
                }
                return result;
            });
        } catch (RconNotYetAuthException e) {
            throw new NotAuthenticatedException(e);
        }
    }

    @Override
    public CompletableFuture<String> tryExecute(ManagedServer managedServer, String command) {
        return CompletableFuture.supplyAsync(() -> {
            for (int retryCount = 1; retryCount <= maxRetries; retryCount++) {
                try {
                    if (!isAuthenticated(managedServer)) {
                        log.info("tryExecute :: Authenticating with server '{}' (Attempts: {})", managedServer.getServerDetails(), retryCount);
                        RconAuthStatus authStatus1 = authenticate(managedServer).join();
                        if (authStatus1 == null || !authStatus1.isAuthenticated()) {
                            log.info("tryExecute :: Not authenticated. Retrying...");
                            continue;
                        }
                    }
                    log.info("tryExecute :: Executing command '{}' on server '{}' (Attempts: {})", command, managedServer.getServerDetails(), retryCount);
                    return execute(managedServer, command).join();
                } catch (ReadTimeoutException | CompletionException | NotAuthenticatedException ex) {
                    log.error("Rcon command execution error {}", ex.getMessage());
                    //log.debug("RconTask :: Server authentication failed: " + managedServer.getServerDetails(), ex);
                }
            }
            RconAuthStatus authStatus1 = getStatus(managedServer);
            throw new CompletionException(new NotAuthenticatedException(String.format("Failed to authenticate with server (Reason: %s)", authStatus1 != null ? authStatus1.getReason() : "N/A")));
        }, rconExecutorService);
    }

    @Override
    public <T> CompletableFuture<T> executeAndParse(ManagedServer managedServer, String command, BiFunction<ManagedServer, String, T> parser) throws NotAuthenticatedException {
        Check.requireNonNull(parser, "Parser cannot be null");
        return execute(managedServer, command).thenApply(result -> parser.apply(managedServer, result));
    }

    @Override
    public <T> CompletableFuture<T> tryExecuteAndParse(ManagedServer managedServer, String command, BiFunction<ManagedServer, String, T> parser) {
        Check.requireNonNull(parser, "Parser cannot be null");
        return tryExecute(managedServer, command).thenApply(result -> parser.apply(managedServer, result));
    }

    @Autowired
    public void setSourceRconClient(SourceRconClient sourceRconClient) {
        this.sourceRconClient = sourceRconClient;
    }

    @Autowired
    public void setRconExecutorService(ExecutorService rconExecutorService) {
        this.rconExecutorService = rconExecutorService;
    }
}
