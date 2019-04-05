package com.ibasco.sourcebuddy.components;

import com.ibasco.agql.protocols.valve.source.query.pojos.SourcePlayer;
import com.ibasco.agql.protocols.valve.steam.webapi.pojos.SteamSourceServer;
import com.ibasco.agql.protocols.valve.steam.webapi.pojos.StoreAppDetails;
import com.ibasco.sourcebuddy.domain.PlayerInfo;
import com.ibasco.sourcebuddy.domain.ServerDetails;
import com.ibasco.sourcebuddy.domain.SteamApp;
import com.ibasco.sourcebuddy.domain.SteamAppDetails;
import com.ibasco.sourcebuddy.enums.OperatingSystem;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class EntityMapper {

    public SteamAppDetails map(StoreAppDetails storeAppDetails, SteamApp app) {
        if (storeAppDetails == null)
            return null;
        SteamAppDetails appDetails = new SteamAppDetails();
        if (app != null)
            appDetails.setSteamApp(app);
        appDetails.setName(storeAppDetails.getName());
        appDetails.setShortDescription(storeAppDetails.getShortDescription());
        appDetails.setDetailedDescription(storeAppDetails.getDetailedDescription());
        appDetails.setHeaderImageUrl(storeAppDetails.getHeaderImageUrl());
        appDetails.setType(storeAppDetails.getType());
        return appDetails;
    }

    public List<SteamApp> mapSteamAppList(List<com.ibasco.agql.protocols.valve.steam.webapi.pojos.SteamApp> steamApps) {
        return steamApps.parallelStream().map(SteamApp::new).collect(Collectors.toList());
    }

    public ServerDetails map(SteamSourceServer server) {
        String[] addr = server.getAddr().split(":");
        ServerDetails details = new ServerDetails(new InetSocketAddress(addr[0], Integer.valueOf(addr[1])));
        details.setName(server.getName());
        details.setGameDirectory(server.getGamedir());
        details.setVersion(server.getVersion());
        details.setPlayerCount(server.getPlayers());
        details.setMaxPlayerCount(server.getMaxPlayers());
        details.setMapName(server.getMap());
        details.setOperatingSystem(OperatingSystem.valueOfStr(server.getOs()));
        details.setSteamId(server.getSteamid());
        details.setSecure(server.getSecure());
        details.setDedicated(server.getDedicated());
        return details;
    }

    public PlayerInfo map(SourcePlayer player) {
        PlayerInfo playerInfo = new PlayerInfo();
        playerInfo.setName(player.getName());
        playerInfo.setIndex(player.getIndex());
        playerInfo.setDuration(player.getDuration());
        playerInfo.setScore(player.getScore());
        return playerInfo;
    }

    public List<PlayerInfo> map(List<SourcePlayer> sourcePlayers) {
        return sourcePlayers.stream().map(this::map).collect(Collectors.toList());
    }
}
