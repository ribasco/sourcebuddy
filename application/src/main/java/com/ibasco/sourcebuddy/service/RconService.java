package com.ibasco.sourcebuddy.service;

import com.ibasco.sourcebuddy.domain.RconStatus;
import com.ibasco.sourcebuddy.domain.ServerDetails;
import com.ibasco.sourcebuddy.exceptions.NotAuthenticatedException;
import com.ibasco.sourcebuddy.util.Check;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

public interface RconService {

    CompletableFuture<RconStatus> authenticate(InetSocketAddress address, String password);

    default boolean isAuthenticated(ServerDetails server) {
        Check.requireNonNull(server, "Server must not be null");
        return isAuthenticated(server.getAddress());
    }

    boolean isAuthenticated(InetSocketAddress address);

    default CompletableFuture<RconStatus> authenticate(ServerDetails server, String password) {
        Check.requireNonNull(server, "Server must not be null");
        Check.requireNonBlank(password, "Password must not be empty");
        return authenticate(server.getAddress(), password);
    }

    CompletableFuture<String> execute(InetSocketAddress address, String command) throws NotAuthenticatedException;

    default CompletableFuture<String> execute(ServerDetails server, String command) throws NotAuthenticatedException {
        Check.requireNonNull(server, "Server must not be null");
        Check.requireNonBlank(command, "Command must not be empty");
        return execute(server.getAddress(), command);
    }
}
