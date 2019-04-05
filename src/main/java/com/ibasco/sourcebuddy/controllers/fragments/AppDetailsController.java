package com.ibasco.sourcebuddy.controllers.fragments;

import com.ibasco.sourcebuddy.controllers.FragmentController;
import com.ibasco.sourcebuddy.domain.SteamAppDetails;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import java.net.URL;
import java.util.ResourceBundle;

@Scope("prototype")
@Controller
public class AppDetailsController extends FragmentController {

    private static final Logger log = LoggerFactory.getLogger(AppDetailsController.class);

    @FXML
    private Label lblGameTitle;

    @FXML
    private Label lblGameDescription;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        log.debug("Got instances: {}, {}", lblGameDescription, lblGameTitle);
    }

    public void updateDetails(SteamAppDetails details) {
        updateTitle(details.getName());
        updateDescription(details.getShortDescription());
        //updateThumbnail(details.getHeaderImage());
    }

    private void updateTitle(String title) {
        lblGameTitle.setText(title);
    }

    private void updateDescription(String description) {
        lblGameDescription.setText(description);
    }
}
