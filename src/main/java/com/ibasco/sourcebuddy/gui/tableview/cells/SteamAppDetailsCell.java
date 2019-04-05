package com.ibasco.sourcebuddy.gui.tableview.cells;

import com.ibasco.sourcebuddy.controllers.fragments.AppDetailsController;
import com.ibasco.sourcebuddy.domain.SteamApp;
import com.ibasco.sourcebuddy.domain.SteamAppDetails;

public class SteamAppDetailsCell extends ViewFragmentCell<SteamApp, SteamAppDetails, AppDetailsController> {

    @Override
    protected void processItem(SteamAppDetails details) {
        getController().updateDetails(details);
        setGraphic(getRootNode());
    }
}
