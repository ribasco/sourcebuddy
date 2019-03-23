package com.ibasco.sourcebuddy.controllers;

import com.ibasco.sourcebuddy.domain.PlayerInfo;
import com.ibasco.sourcebuddy.domain.ServerDetails;
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
    private TableView<PlayerInfo> tvPlayerTable;

    private ServerDetailsModel serverDetailsModel;

    @Override
    public void initialize(Stage stage, Node rootNode) {
        setupPlayerInfoTable();
        setupContextMenu();
        serverDetailsModel.getServerSelectionModel().selectedItemProperty().addListener(this::updatePlayerTableOnSelection);
    }

    private void setupPlayerInfoTable() {
        TableColumn<PlayerInfo, String> indexCol = new TableColumn<>("Index");
        indexCol.setCellValueFactory(new PropertyValueFactory<>("index"));

        TableColumn<PlayerInfo, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<PlayerInfo, String> scoreCol = new TableColumn<>("Score");
        scoreCol.setCellValueFactory(new PropertyValueFactory<>("score"));

        TableColumn<PlayerInfo, String> durationCol = new TableColumn<>("Duration");
        durationCol.setCellValueFactory(new PropertyValueFactory<>("duration"));

        //noinspection unchecked
        tvPlayerTable.getColumns().addAll(indexCol, nameCol, scoreCol, durationCol);
        tvPlayerTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    private void setupContextMenu() {
        ContextMenu cMenu = new ContextMenu();

        MenuItem miCopyName = new MenuItem("Copy Player Name(s)");
        miCopyName.setOnAction(this::copyPlayerNames);

        cMenu.getItems().add(miCopyName);
        tvPlayerTable.setContextMenu(cMenu);
    }

    private void updatePlayerTableOnSelection(ObservableValue observableValue, ServerDetails o, ServerDetails newValue) {
        if (newValue == null)
            return;
        if (!serverDetailsModel.READ_LOCK.tryLock()) {
            log.debug("Unable to acquire read lock for players");
            return;
        }
        try {
            if (newValue.getPlayers() != null && !newValue.getPlayers().isEmpty()) {
                tvPlayerTable.setItems(newValue.getPlayers());
            } else {
                tvPlayerTable.setItems(null);
            }
        } finally {
            serverDetailsModel.READ_LOCK.unlock();
        }
    }

    private void copyPlayerNames(ActionEvent actionEvent) {
        ObservableList<PlayerInfo> selectedPlayers = tvPlayerTable.getSelectionModel().getSelectedItems();
        ClipboardContent content = new ClipboardContent();
        StringBuilder namesBuilder = new StringBuilder();
        int ctr = 0;
        for (PlayerInfo playerInfo : selectedPlayers) {
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

    @Autowired
    public void setServerDetailsModel(ServerDetailsModel serverDetailsModel) {
        this.serverDetailsModel = serverDetailsModel;
    }
}
