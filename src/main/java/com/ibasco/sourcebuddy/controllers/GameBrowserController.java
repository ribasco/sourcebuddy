package com.ibasco.sourcebuddy.controllers;

import com.ibasco.sourcebuddy.domain.SteamApp;
import com.ibasco.sourcebuddy.domain.SteamAppDetails;
import com.ibasco.sourcebuddy.service.SourceServerQueryService;
import com.ibasco.sourcebuddy.service.SteamQueryService;
import static com.ibasco.sourcebuddy.util.GuiUtil.createBasicColumn;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.controlsfx.control.MasterDetailPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.function.BiConsumer;

@Controller
public class GameBrowserController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(GameBrowserController.class);

    //<editor-fold desc="FXML Properties">
    @FXML
    private TableView<SteamApp> tvGameBrowser;

    @FXML
    private MasterDetailPane mdpGameBrowser;
    //</editor-fold>

    private SteamQueryService steamQueryService;

    private SourceServerQueryService sourceServerQueryService;

    @Override
    public void initialize(Stage stage, Node rootNode) {
        log.debug("Game browser initialized: {}", stage);
        setupGameBrowserTable();
        steamQueryService.findSteamApps().whenComplete((steamApps, throwable) -> {
            if (throwable != null) {
                log.error("Error", throwable);
                return;
            }
            log.debug("Got total of {} apps", steamApps.size());
            tvGameBrowser.setItems(FXCollections.observableArrayList(steamApps));
            log.debug("Saving to repository");
        });
    }

    private void setupGameBrowserTable() {
        tvGameBrowser.getColumns().clear();

        createBasicColumn(tvGameBrowser, "App ID", "id");
        createBasicColumn(tvGameBrowser, "Name", "name");
        tvGameBrowser.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<SteamApp>() {
            @Override
            public void changed(ObservableValue<? extends SteamApp> observable, SteamApp oldValue, SteamApp newValue) {
                if (newValue != null) {
                    steamQueryService.findAppDetails(newValue).whenComplete(new BiConsumer<SteamAppDetails, Throwable>() {
                        @Override
                        public void accept(SteamAppDetails store, Throwable throwable) {
                            if (throwable != null) {
                                log.error("err", throwable);
                                return;
                            }
                            if (store != null) {
                                log.info("Name: {}, Desc: {}, Type: {}, Image URL: {}", store.getName(), store.getShortDescription(), store.getType(), store.getHeaderImageUrl());
                                Image img = new Image(store.getHeaderImageUrl());
                            } else
                                log.debug("Not available");
                        }
                    });
                }
            }
        });
        //Raise the following events on item selection
        tvGameBrowser.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    @Autowired
    public void setSteamQueryService(SteamQueryService steamQueryService) {
        this.steamQueryService = steamQueryService;
    }

    @Autowired
    public void setSourceServerQueryService(SourceServerQueryService sourceServerQueryService) {
        this.sourceServerQueryService = sourceServerQueryService;
    }

}
