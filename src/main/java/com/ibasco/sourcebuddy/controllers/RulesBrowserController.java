package com.ibasco.sourcebuddy.controllers;

import com.ibasco.sourcebuddy.entities.KeyValueInfo;
import com.ibasco.sourcebuddy.model.ServerDetailsModel;
import com.ibasco.sourcebuddy.util.GuiUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
public class RulesBrowserController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(RulesBrowserController.class);

    @FXML
    private TableView<KeyValueInfo> tvRulesTable;

    private ServerDetailsModel serverDetailsModel;

    @Override
    public void initialize(Stage stage, Node rootNode) {
        GuiUtil.setupKeyValueTable(tvRulesTable);
        serverDetailsModel.getServerSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue.getRules() != null) {
                ObservableList<KeyValueInfo> rulesList = FXCollections.observableArrayList();
                for (Map.Entry<String, String> e : newValue.getRules().entrySet()) {
                    rulesList.add(new KeyValueInfo(e.getKey(), e.getValue()));
                }
                tvRulesTable.setItems(rulesList);
            } else {
                tvRulesTable.setItems(null);
            }
        });
    }

    @Autowired
    public void setServerDetailsModel(ServerDetailsModel serverDetailsModel) {
        this.serverDetailsModel = serverDetailsModel;
    }
}
