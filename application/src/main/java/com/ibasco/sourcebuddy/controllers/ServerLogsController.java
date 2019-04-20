package com.ibasco.sourcebuddy.controllers;

import com.ibasco.sourcebuddy.model.ServerDetailsModel;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;

public class ServerLogsController extends BaseController {

    @FXML
    private TextArea taLogs;

    private ServerDetailsModel serverDetailsModel;

    @Override
    public void initialize(Stage stage, Node rootNode) {

    }

    @Autowired
    public void setServerDetailsModel(ServerDetailsModel serverDetailsModel) {
        this.serverDetailsModel = serverDetailsModel;
    }
}
