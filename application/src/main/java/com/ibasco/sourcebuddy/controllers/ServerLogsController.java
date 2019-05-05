package com.ibasco.sourcebuddy.controllers;

import com.ibasco.sourcebuddy.model.AppModel;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;

public class ServerLogsController extends BaseController {

    @FXML
    private TextArea taLogs;

    private AppModel appModel;

    @Override
    public void initialize(Stage stage, Node rootNode) {

    }

    @Autowired
    public void setAppModel(AppModel appModel) {
        this.appModel = appModel;
    }
}
