package com.ibasco.sourcebuddy.controllers;

import com.ibasco.sourcebuddy.model.ServerDetailsModel;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class ServerChatController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(ServerChatController.class);

    @FXML
    private TabPane tpChat;

    private ServerDetailsModel serverDetailsModel;

    @Override
    public void initialize(Stage stage, Node rootNode) {

    }

    @Autowired
    public void setServerDetailsModel(ServerDetailsModel serverDetailsModel) {
        this.serverDetailsModel = serverDetailsModel;
    }
}
