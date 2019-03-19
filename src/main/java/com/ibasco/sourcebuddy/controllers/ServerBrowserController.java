package com.ibasco.sourcebuddy.controllers;

import com.ibasco.sourcebuddy.entities.SourceServerDetails;
import com.ibasco.sourcebuddy.model.ServerDetailsModel;
import com.ibasco.sourcebuddy.service.ServerDetailsUpdateService;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.controlsfx.control.MasterDetailPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.net.InetSocketAddress;

@SuppressWarnings("Duplicates")
@Controller
public class ServerBrowserController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(ServerBrowserController.class);

    @FXML
    private MasterDetailPane mdpServerBrowser;

    @FXML
    private TableView<SourceServerDetails> serverListTable;

    private ServerDetailsModel serverDetailsModel;

    private ServerDetailsUpdateService serverInfoUpdateService;

    @Override
    public void initialize(Stage stage, Node rootNode) {
        log.info("Initializing server browser controller");
        log.info("Got: {}", mdpServerBrowser);

        setupServerBrowserTable();

        serverListTable.itemsProperty().bind(serverDetailsModel.serverDetailsProperty());
        serverDetailsModel.statusMessageProperty().bind(serverInfoUpdateService.messageProperty());
        serverDetailsModel.serverSelectionModelProperty().bind(serverListTable.selectionModelProperty());
        serverInfoUpdateService.setServerDetailsModel(serverDetailsModel);
        serverInfoUpdateService.start();
    }

    private void setupServerBrowserTable() {
        TableColumn<SourceServerDetails, String> nameCol = new TableColumn<>("Server Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<SourceServerDetails, InetSocketAddress> ipCol = new TableColumn<>("IP/Port");
        ipCol.setCellValueFactory(new PropertyValueFactory<>("address"));
        ipCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(InetSocketAddress item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    if (item != null && item.getAddress() != null)
                        setText(item.getAddress().getHostAddress() + ":" + item.getPort());
                }
            }
        });
        TableColumn<SourceServerDetails, Integer> playerCountCol = new TableColumn<>("Player Count");
        playerCountCol.setCellValueFactory(new PropertyValueFactory<>("playerCount"));

        TableColumn<SourceServerDetails, Integer> maxPlayerCountCol = new TableColumn<>("Max Players");
        maxPlayerCountCol.setCellValueFactory(new PropertyValueFactory<>("maxPlayerCount"));

        TableColumn<SourceServerDetails, String> mapNameCol = new TableColumn<>("Current Map");
        mapNameCol.setCellValueFactory(new PropertyValueFactory<>("mapName"));

        TableColumn<SourceServerDetails, String> serverTagsCol = new TableColumn<>("Server Tags");
        serverTagsCol.setCellValueFactory(new PropertyValueFactory<>("serverTags"));

        TableColumn<SourceServerDetails, String> appIdCol = new TableColumn<>("App ID");
        appIdCol.setCellValueFactory(new PropertyValueFactory<>("appId"));

        TableColumn<SourceServerDetails, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        TableColumn<SourceServerDetails, String> lastUpdateCol = new TableColumn<>("Last Updated");
        lastUpdateCol.setCellValueFactory(new PropertyValueFactory<>("lastUpdate"));

        //Raise the following events on item selection
        serverListTable.getSelectionModel().selectedItemProperty().addListener(this::processNewItemSelection);
        serverListTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        serverListTable.getColumns().clear();
        //noinspection unchecked
        serverListTable.getColumns().addAll(nameCol, ipCol, playerCountCol, maxPlayerCountCol, mapNameCol, statusCol, appIdCol, serverTagsCol, lastUpdateCol);
    }

    private void processNewItemSelection(ObservableValue<? extends SourceServerDetails> observable, SourceServerDetails oldInfo, SourceServerDetails newInfo) {
        if (newInfo == null)
            return;

        if (newInfo.getPlayers() != null && !newInfo.getPlayers().isEmpty()) {
            //tvServerPlayerInfo.setItems(newInfo.getPlayers());
        } else {
            //tvServerPlayerInfo.setItems(null);
        }
    }

    /*private void setupMasterServerToolbarComponents() {
        tfSearchCriteria.textProperty().addListener((observable, oldValue, newValue) -> {
            Predicate<SourceServerDetails> criteria = info -> {
                if (newValue == null || newValue.isBlank())
                    return true;
                String search = newValue.toLowerCase();
                String ipPort = info.getAddress().getAddress().getHostAddress() + ":" + info.getAddress().getPort();
                return info.getName().toLowerCase().contains(search) || info.getMapName().toLowerCase().contains(search) || ipPort.contains(search);
            };
            synchronized (lock) {
                filteredMasterServers.setPredicate(criteria);
            }
        });
    }*/

    @Autowired
    public void setServerInfoUpdateService(ServerDetailsUpdateService serverInfoUpdateService) {
        this.serverInfoUpdateService = serverInfoUpdateService;
    }

    @Autowired
    public void setServerDetailsModel(ServerDetailsModel serverDetailsModel) {
        this.serverDetailsModel = serverDetailsModel;
    }
}
