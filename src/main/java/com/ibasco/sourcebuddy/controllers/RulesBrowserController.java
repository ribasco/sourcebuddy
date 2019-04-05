package com.ibasco.sourcebuddy.controllers;

import com.ibasco.sourcebuddy.components.GuiHelper;
import com.ibasco.sourcebuddy.domain.KeyValueInfo;
import com.ibasco.sourcebuddy.domain.ServerDetails;
import com.ibasco.sourcebuddy.model.ServerDetailsModel;
import javafx.beans.value.ObservableValue;
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
        GuiHelper.setupKeyValueTable(tvRulesTable);
        serverDetailsModel.getServerSelectionModel().selectedItemProperty().addListener(this::updateRulesSelection);
    }

    private void updateRulesSelection(ObservableValue observableValue, ServerDetails oldValue, ServerDetails newValue) {
        if (!ServerDetailsModel.READ_LOCK.tryLock()) {
            log.debug("Unable to acquire read lock for rules");
            return;
        }
        try {
            if (newValue != null && newValue.getRules() != null) {
                ObservableList<KeyValueInfo> rulesList = FXCollections.observableArrayList();
                for (Map.Entry<String, String> e : newValue.getRules().entrySet()) {
                    rulesList.add(new KeyValueInfo(e.getKey(), e.getValue()));
                }
                tvRulesTable.setItems(rulesList);
            } else {
                tvRulesTable.setItems(null);
            }
        } finally {
            ServerDetailsModel.READ_LOCK.unlock();
        }
    }

    @Autowired
    public void setServerDetailsModel(ServerDetailsModel serverDetailsModel) {
        this.serverDetailsModel = serverDetailsModel;
    }
}
