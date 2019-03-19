package com.ibasco.sourcebuddy.controllers;

import com.ibasco.sourcebuddy.entities.SourcePlayerInfo;
import com.ibasco.sourcebuddy.entities.SourceServerDetails;
import com.ibasco.sourcebuddy.model.ServerDetailsModel;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@SuppressWarnings("Duplicates")
@Controller
public class PlayerBrowserController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(PlayerBrowserController.class);

    @FXML
    private TableView<SourcePlayerInfo> tvPlayerTable;

    private ServerDetailsModel serverDetailsModel;

    @Override
    public void initialize(Stage stage, Node rootNode) {
        setupPlayerInfoTable();
        setupContextMenu();
        serverDetailsModel.getServerSelectionModel().selectedItemProperty().addListener(this::updatePlayerTableOnSelection);
    }

    private void updatePlayerTableOnSelection(ObservableValue observableValue, SourceServerDetails o, SourceServerDetails newValue) {
        if (newValue == null)
            return;
        if (newValue.getPlayers() != null && !newValue.getPlayers().isEmpty()) {
            tvPlayerTable.setItems(newValue.getPlayers());
        } else {
            tvPlayerTable.setItems(null);
        }
    }

    private void setupContextMenu() {
        ContextMenu cMenu = new ContextMenu();

        MenuItem miCopyName = new MenuItem("Copy Player Name(s)");
        miCopyName.setOnAction(this::copyPlayerNames);

        cMenu.getItems().add(miCopyName);
        tvPlayerTable.setContextMenu(cMenu);
    }

    private void copyPlayerNames(ActionEvent actionEvent) {
        ObservableList<SourcePlayerInfo> selectedPlayers = tvPlayerTable.getSelectionModel().getSelectedItems();
        ClipboardContent content = new ClipboardContent();
        StringBuilder namesBuilder = new StringBuilder();
        int ctr = 0;
        for (SourcePlayerInfo playerInfo : selectedPlayers) {
            if (playerInfo.getName() != null && !playerInfo.getName().isBlank()) {
                namesBuilder.append(playerInfo.getName());
                namesBuilder.append("\n");
                ctr++;
            }
        }
        if (!namesBuilder.toString().isBlank()) {
            content.putString(namesBuilder.toString());
            Clipboard.getSystemClipboard().setContent(content);
        }
        log.debug("Copied total of {} player names", ctr);
    }

    private void setupPlayerInfoTable() {
        TableColumn<SourcePlayerInfo, String> indexCol = new TableColumn<>("Index");
        indexCol.setCellValueFactory(new PropertyValueFactory<>("index"));

        TableColumn<SourcePlayerInfo, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<SourcePlayerInfo, String> scoreCol = new TableColumn<>("Score");
        scoreCol.setCellValueFactory(new PropertyValueFactory<>("score"));

        TableColumn<SourcePlayerInfo, String> durationCol = new TableColumn<>("Duration");
        durationCol.setCellValueFactory(new PropertyValueFactory<>("duration"));

        //noinspection unchecked
        tvPlayerTable.getColumns().addAll(indexCol, nameCol, scoreCol, durationCol);
        tvPlayerTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    @Autowired
    public void setServerDetailsModel(ServerDetailsModel serverDetailsModel) {
        this.serverDetailsModel = serverDetailsModel;
    }
}
