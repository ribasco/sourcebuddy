package com.ibasco.sourcebuddy.sourcebuddy.controllers;

import com.ibasco.agql.protocols.valve.source.query.client.SourceQueryClient;
import com.ibasco.agql.protocols.valve.steam.master.MasterServerFilter;
import com.ibasco.agql.protocols.valve.steam.master.client.MasterServerQueryClient;
import com.ibasco.agql.protocols.valve.steam.master.enums.MasterServerRegion;
import com.ibasco.agql.protocols.valve.steam.master.enums.MasterServerType;
import com.ibasco.sourcebuddy.sourcebuddy.model.SourceKeyValueInfo;
import com.ibasco.sourcebuddy.sourcebuddy.model.SourcePlayerInfo;
import com.ibasco.sourcebuddy.sourcebuddy.model.SourceServerInfo;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.InetSocketAddress;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class MainController implements Initializable {

    private static final Logger log = LoggerFactory.getLogger(MainController.class);

    //region FXML Properties
    @FXML
    public TableView<SourcePlayerInfo> tvServerPlayerInfo;

    @FXML
    public TableView<SourceKeyValueInfo> tvServerDetails;

    @FXML
    public TableView<SourceKeyValueInfo> tvServerRules;

    @FXML
    public TextArea taServerLog;

    @FXML
    public TextField tfRcon;

    @FXML
    private Button btnListServers;

    @FXML
    private TableView<SourceServerInfo> tvServerList;
    //endregion

    private SourceQueryClient serverQueryClient;

    private MasterServerQueryClient masterQueryClient;

    private ObservableList<SourceServerInfo> serverList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        log.info("Main controller initialized");
        setupMasterServerTable();
        setupPlayerInfoTable();
        setupKeyValueTable(tvServerDetails);
        setupKeyValueTable(tvServerRules);
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
        tvServerPlayerInfo.getColumns().addAll(indexCol, nameCol, scoreCol, durationCol);
    }

    private void setupKeyValueTable(TableView<SourceKeyValueInfo> tableView) {
        TableColumn<SourceKeyValueInfo, String> nameCol = new TableColumn<>("Property");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("key"));

        TableColumn<SourceKeyValueInfo, String> valueCol = new TableColumn<>("Value");
        valueCol.setCellValueFactory(new PropertyValueFactory<>("value"));

        //noinspection unchecked
        tableView.getColumns().addAll(nameCol, valueCol);
    }

    private void setupMasterServerTable() {
        TableColumn<SourceServerInfo, String> nameCol = new TableColumn<>("Server Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<SourceServerInfo, InetSocketAddress> ipCol = new TableColumn<>("IP/Port");
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
        TableColumn<SourceServerInfo, Integer> playerCountCol = new TableColumn<>("Player Count");
        playerCountCol.setCellValueFactory(new PropertyValueFactory<>("playerCount"));

        TableColumn<SourceServerInfo, Integer> maxPlayerCountCol = new TableColumn<>("Max Players");
        maxPlayerCountCol.setCellValueFactory(new PropertyValueFactory<>("maxPlayerCount"));

        TableColumn<SourceServerInfo, String> mapNameCol = new TableColumn<>("Current Map");
        mapNameCol.setCellValueFactory(new PropertyValueFactory<>("mapName"));

        TableColumn<SourceServerInfo, String> serverTagsCol = new TableColumn<>("Server Tags");
        serverTagsCol.setCellValueFactory(new PropertyValueFactory<>("serverTags"));

        //Raise the following events on item selection
        tvServerList.getSelectionModel().selectedItemProperty().addListener((observableValue, oldVal, newVal) -> {
            if (oldVal != null && tvServerPlayerInfo.itemsProperty().isBound())
                tvServerPlayerInfo.itemsProperty().unbind();
            if (newVal == null)
                return;

            updateSourceServerInfo(newVal);
            populatePlayerDetailsTable(newVal);
            populateServerDetailsTable(newVal);
            populateServerRulesTable(newVal);
        });

        tvServerList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        tvServerList.getColumns().clear();
        //noinspection unchecked
        tvServerList.getColumns().addAll(nameCol, ipCol, playerCountCol, maxPlayerCountCol, mapNameCol, serverTagsCol);

        btnListServers.setOnAction(this::refreshMasterServerList);
    }

    private void updateSourceServerInfo(SourceServerInfo info) {
        serverQueryClient.getServerInfo(info.getAddress()).thenAccept(update -> {
            info.setName(update.getName());
            info.setPlayerCount(update.getNumOfPlayers());
            info.setMaxPlayerCount(update.getMaxPlayers());
            info.setMapName(update.getMapName());
        });
    }

    private void refreshMasterServerList(ActionEvent actionEvent) {
        log.info("Listing all servers: {}", serverQueryClient);
        tvServerList.getItems().clear();
        tvServerList.setItems(serverList);

        MasterServerFilter filter = MasterServerFilter.create().dedicated(true).allServers().appId(550);
        masterQueryClient.getServerList(MasterServerType.SOURCE, MasterServerRegion.REGION_ALL, filter, this::populateMasterServerListItem)
                .whenComplete((inetSocketAddresses, throwable) -> log.info("Done"));
    }

    private void populateServerDetailsTable(final SourceServerInfo info) {
        tvServerDetails.getItems().clear();
        addProperty(tvServerDetails, "IP Address", info.getAddress().getAddress().getHostAddress());
        addProperty(tvServerDetails, "Port", info.getAddress().getPort());
        addProperty(tvServerDetails, "Name", info.getName());
        addProperty(tvServerDetails, "Num of Players", info.getPlayerCount());
        addProperty(tvServerDetails, "Max Players", info.getMaxPlayerCount());
        addProperty(tvServerDetails, "Game Description", info.getDescription());
        addProperty(tvServerDetails, "Game ID", info.getGameId());
        addProperty(tvServerDetails, "Map Name", info.getMapName());
        addProperty(tvServerDetails, "App ID", info.getAppId());
        addProperty(tvServerDetails, "Server Tags", info.getServerTags());
        addProperty(tvServerDetails, "Operating System", info.getOperatingSystem());
        addProperty(tvServerDetails, "Game Directory", info.getGameDirectory());
    }

    private void populateServerRulesTable(final SourceServerInfo info) {
        tvServerRules.getItems().clear();
        serverQueryClient.getServerRulesCached(info.getAddress()).thenAccept(rulesMap -> {
            for (var entry : rulesMap.entrySet())
                addProperty(tvServerRules, entry.getKey(), entry.getValue());
        });
    }

    private void populatePlayerDetailsTable(final SourceServerInfo info) {
        serverQueryClient.getPlayersCached(info.getAddress()).thenAccept(sourcePlayers -> {
            ObservableList<SourcePlayerInfo> playerInfoList = sourcePlayers
                    .stream()
                    .map(SourcePlayerInfo::new)
                    .collect(Collectors.toCollection(FXCollections::observableArrayList));

            tvServerPlayerInfo.itemsProperty().bind(info.playersProperty());
            info.setPlayers(playerInfoList);
        });
    }

    private void populateMasterServerListItem(InetSocketAddress address, InetSocketAddress sender, Throwable throwable) {
        serverQueryClient.getServerInfo(address).thenAccept(details -> {
            log.debug("INFO: {}", details);
            details.setAddress(address);
            serverList.add(new SourceServerInfo(details));
        });
    }

    private void addProperty(TableView<SourceKeyValueInfo> table, String name, Object value) {
        table.getItems().add(new SourceKeyValueInfo(name, value));
    }

    @Autowired
    public void setMasterQueryClient(MasterServerQueryClient masterQueryClient) {
        this.masterQueryClient = masterQueryClient;
    }

    @Autowired
    public void setServerQueryClient(SourceQueryClient serverQueryClient) {
        this.serverQueryClient = serverQueryClient;
    }
}
