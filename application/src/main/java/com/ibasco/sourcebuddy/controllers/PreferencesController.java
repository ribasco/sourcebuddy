package com.ibasco.sourcebuddy.controllers;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TreeView;
import javafx.stage.Stage;
import org.controlsfx.control.NotificationPane;

public class PreferencesController extends BaseController {

    @FXML
    private TreeView tvSettings;

    @FXML
    private NotificationPane npMain;

    @Override
    public void initialize(Stage stage, Node rootNode) {

    }
}
