package com.ibasco.sourcebuddy.service;

import com.ibasco.sourcebuddy.domain.ManagedServer;
import com.ibasco.sourcebuddy.domain.RconAuthStatus;
import com.ibasco.sourcebuddy.exceptions.NotAuthenticatedException;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

public interface RconService {

    CompletableFuture<RconAuthStatus> authenticate(ManagedServer managedServer);

    boolean isAuthenticated(ManagedServer managedServer);

    RconAuthStatus getStatus(ManagedServer managedServer);

    CompletableFuture<String> execute(ManagedServer managedServer, String command) throws NotAuthenticatedException;

    CompletableFuture<String> tryExecute(ManagedServer managedServer, String command);

    <T> CompletableFuture<T> executeAndParse(ManagedServer managedServer, String command, BiFunction<ManagedServer, String, T> parser) throws NotAuthenticatedException;

    <T> CompletableFuture<T> tryExecuteAndParse(ManagedServer managedServer, String command, BiFunction<ManagedServer, String, T> parser);

}
