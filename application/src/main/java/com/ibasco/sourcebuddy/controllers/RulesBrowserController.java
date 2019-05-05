package com.ibasco.sourcebuddy.controllers;

import com.ibasco.sourcebuddy.components.GuiHelper;
import com.ibasco.sourcebuddy.domain.KeyValueInfo;
import com.ibasco.sourcebuddy.domain.ServerDetails;
import com.ibasco.sourcebuddy.model.AppModel;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.stream.Collectors;

public class RulesBrowserController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(RulesBrowserController.class);

    @FXML
    private TableView<KeyValueInfo> tvRulesTable;

    private AppModel appModel;

    @Override
    public void initialize(Stage stage, Node rootNode) {
        GuiHelper.setupKeyValueTable(tvRulesTable);
        appModel.selectedServerProperty().addListener(this::updateOnSelection);
        tvRulesTable.setPlaceholder(new Label("N/A"));
    }

    private void updateOnSelection(ObservableValue<? extends ServerDetails> observableValue, ServerDetails oldValue, ServerDetails newValue) {
        if (oldValue != null && oldValue.rulesProperty().isBound()) {
            oldValue.rulesProperty().unbind();
        }
        if (newValue != null) {
            ObjectBinding<ObservableList<KeyValueInfo>> ob = Bindings.createObjectBinding(() -> {
                if (newValue.getRules() != null) {
                    return newValue.getRules().entrySet().stream().map(e -> new KeyValueInfo(e.getKey(), e.getValue())).collect(Collectors.toCollection(FXCollections::observableArrayList));
                }
                return FXCollections.observableArrayList();
            }, newValue.rulesProperty());
            tvRulesTable.itemsProperty().bind(ob);
            tvRulesTable.refresh();
        } else {
            tvRulesTable.itemsProperty().unbind();
            tvRulesTable.setItems(null);
        }
    }

    private void updateRulesSelection(ObservableValue observableValue, ServerDetails oldValue, ServerDetails newValue) {
        if (newValue != null && newValue.getRules() != null) {
            ObservableList<KeyValueInfo> rulesList = FXCollections.observableArrayList();
            for (Map.Entry<String, String> e : newValue.getRules().entrySet()) {
                rulesList.add(new KeyValueInfo(e.getKey(), e.getValue()));
            }
            tvRulesTable.setItems(rulesList);
        } else {
            tvRulesTable.setItems(null);
        }
    }

    @Autowired
    public void setAppModel(AppModel appModel) {
        this.appModel = appModel;
    }
}
