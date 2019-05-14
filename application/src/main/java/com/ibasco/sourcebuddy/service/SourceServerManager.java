package com.ibasco.sourcebuddy.service;

import com.ibasco.sourcebuddy.components.rcon.*;
import com.ibasco.sourcebuddy.domain.ManagedServer;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface SourceServerManager extends ServerManager {

    CompletableFuture<SourceServerStatus> getServerStatus(ManagedServer server);

    CompletableFuture<List<SourceModPlugin>> getPlugins(ManagedServer server);

    CompletableFuture<Void> reloadPlugins(ManagedServer server);

    CompletableFuture<SourceModPlugin> updatePluginInfo(SourceModPlugin plugin);

    CompletableFuture<Void> reloadPlugin(SourceModPlugin plugin);

    CompletableFuture<Void> unloadAllPlugins(ManagedServer server);

    CompletableFuture<Void> unloadPlugin(SourceModPlugin plugin);

    CompletableFuture<List<SourceModCvar>> getPluginCvars(SourceModPlugin plugin);

    CompletableFuture<List<SourceModExtension>> getExtensions(ManagedServer server);

    CompletableFuture<List<SourceModCommand>> getCommands(SourceModPlugin plugin);

    default CompletableFuture<SourceModCvarChangeResult> updateConvar(ManagedServer server, SourceModCvar cvar, Object value) {
        if (StringUtils.isBlank(cvar.getName()))
            throw new IllegalStateException("Convar name must not be blank");
        return updateConvar(server, cvar.getName(), value);
    }

    CompletableFuture<Void> protectConvar(ManagedServer server, String cvar);

    CompletableFuture<SourceModCvarChangeResult> updateConvar(ManagedServer server, String cvar, Object value);
}
