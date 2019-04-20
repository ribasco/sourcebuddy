package com.ibasco.sourcebuddy.controllers;

import com.ibasco.sourcebuddy.model.SteamGamesModel;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class AddGameController extends DialogController {

    private static final Logger log = LoggerFactory.getLogger(AddGameController.class);

    @FXML
    private TextField tfSearch;

    @FXML
    private TableView tvSteamGames;

    @FXML
    private TableColumn colAppID;

    @FXML
    private TableColumn colName;

    private SteamGamesModel steamGamesModel;

    @Override
    public void initialize(Stage stage, Node rootNode) {

    }

    @Autowired
    public void setSteamGamesModel(SteamGamesModel steamGamesModel) {
        this.steamGamesModel = steamGamesModel;
    }
}
