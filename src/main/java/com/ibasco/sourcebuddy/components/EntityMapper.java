package com.ibasco.sourcebuddy.components;

import com.ibasco.agql.protocols.valve.steam.webapi.pojos.SteamSourceServer;
import com.ibasco.agql.protocols.valve.steam.webapi.pojos.StoreAppDetails;
import com.ibasco.sourcebuddy.domain.ServerDetails;
import com.ibasco.sourcebuddy.domain.SteamApp;
import com.ibasco.sourcebuddy.domain.SteamAppDetails;
import com.ibasco.sourcebuddy.enums.OperatingSystem;
import com.ibasco.sourcebuddy.service.SteamQueryService;
import javafx.collections.FXCollections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class EntityMapper {

    private SteamQueryService steamQueryService;

    public SteamAppDetails convert(StoreAppDetails storeAppDetails) {
        if (storeAppDetails == null)
            return null;
        SteamAppDetails appDetails = new SteamAppDetails();
        appDetails.setName(storeAppDetails.getName());
        appDetails.setShortDescription(storeAppDetails.getShortDescription());
        appDetails.setDetailedDescription(storeAppDetails.getDetailedDescription());
        appDetails.setHeaderImageUrl(storeAppDetails.getHeaderImageUrl());
        appDetails.setType(storeAppDetails.getType());
        return appDetails;
    }

    public List<SteamApp> convert(List<com.ibasco.agql.protocols.valve.steam.webapi.pojos.SteamApp> steamApps) {
        return steamApps.parallelStream().map(SteamApp::new).collect(Collectors.toCollection(FXCollections::observableArrayList));
    }

    public ServerDetails convert(SteamSourceServer server) {
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

    @Autowired
    public void setSteamQueryService(SteamQueryService steamQueryService) {
        this.steamQueryService = steamQueryService;
    }
}
