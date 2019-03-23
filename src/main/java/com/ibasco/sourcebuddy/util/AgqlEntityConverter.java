package com.ibasco.sourcebuddy.util;

import com.ibasco.agql.protocols.valve.steam.webapi.pojos.StoreAppDetails;
import com.ibasco.sourcebuddy.domain.SteamApp;
import com.ibasco.sourcebuddy.domain.SteamAppDetails;
import javafx.collections.FXCollections;

import java.util.List;
import java.util.stream.Collectors;

public class AgqlEntityConverter {

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

}
