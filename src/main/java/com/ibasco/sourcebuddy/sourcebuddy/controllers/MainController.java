package com.ibasco.sourcebuddy.sourcebuddy.controllers;

import com.google.gson.Gson;
import com.ibasco.agql.protocols.valve.source.query.client.SourceQueryClient;
import com.ibasco.agql.protocols.valve.source.query.client.SourceRconClient;
import com.ibasco.agql.protocols.valve.source.query.exceptions.RconNotYetAuthException;
import com.ibasco.agql.protocols.valve.source.query.logger.SourceLogEntry;
import com.ibasco.agql.protocols.valve.source.query.logger.SourceLogListenService;
import com.ibasco.agql.protocols.valve.steam.master.MasterServerFilter;
import com.ibasco.agql.protocols.valve.steam.master.client.MasterServerQueryClient;
import com.ibasco.agql.protocols.valve.steam.master.enums.MasterServerRegion;
import com.ibasco.agql.protocols.valve.steam.master.enums.MasterServerType;
import com.ibasco.sourcebuddy.sourcebuddy.model.SourceKeyValueInfo;
import com.ibasco.sourcebuddy.sourcebuddy.model.SourcePlayerInfo;
import com.ibasco.sourcebuddy.sourcebuddy.model.SourceServerInfo;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
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

    @FXML
    private TextField tfSearchCriteria;

    @FXML
    private ProgressIndicator piMasterServer;
    //endregion

    private Gson gson = new Gson();

    private SourceQueryClient serverQueryClient;

    private MasterServerQueryClient masterQueryClient;

    private SourceRconClient rconClient;

    private SourceLogListenService sourceLogListenService;

    private ObservableList<SourceServerInfo> masterServerList = FXCollections.observableArrayList();

    private FilteredList<SourceServerInfo> filteredMasterServers = masterServerList.filtered(p -> true);

    private CompletableFuture<SourceServerInfo> serverInfoFuture = null;

    private CompletableFuture<Vector<InetSocketAddress>> masterServerListFuture = null;

    private Map<InetSocketAddress, Integer> rconAttempts = new HashMap<>();

    private final Object lock = new Object();

    private InetAddress publicIp;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        log.info("Main controller initialized");
        setupMasterServerTable();
        setupMasterServerToolbarComponents();
        setupPlayerInfoTable();
        setupKeyValueTable(tvServerDetails);
        setupKeyValueTable(tvServerRules);
        setupMasterServerContextMenu();

        tfRcon.addEventHandler(KeyEvent.KEY_RELEASED, event -> {
            if (event.getCode() == KeyCode.ENTER) {
                SourceServerInfo info = tvServerList.getSelectionModel().getSelectedItem();
                if (info != null) {
                    String command = tfRcon.getText();
                    if (command == null || command.isBlank()) {
                        log.warn("No command entered");
                        return;
                    }
                    executeRconCommand(command).thenAccept(s -> tfRcon.setText(""));
                }
            }
        });
    }

    private void setupMasterServerContextMenu() {
        tvServerList.setRowFactory(param -> {
            final TableRow<SourceServerInfo> row = new TableRow<>();
            final ContextMenu rowMenu = new ContextMenu();
            MenuItem miRcon = new MenuItem("RCON connect");
            miRcon.setOnAction(event -> {

            });
            MenuItem miRefresh = new MenuItem("Refresh Server");
            rowMenu.getItems().addAll(miRcon, miRefresh);
            // only display context menu for non-null items:
            row.contextMenuProperty().bind(Bindings.when(Bindings.isNotNull(row.itemProperty())).then(rowMenu).otherwise((ContextMenu) null));
            return row;
        });
    }

    private CompletableFuture<String> executeRconCommand(String command) {
        SourceServerInfo selectedServer = tvServerList.getSelectionModel().getSelectedItem();
        if (selectedServer == null)
            return CompletableFuture.failedFuture(new IllegalStateException("No server selected"));

        try {
            return rconClient.execute(selectedServer.getAddress(), command).thenApply(s -> {
                taServerLog.appendText(s);
                return s;
            });
        } catch (RconNotYetAuthException e) {
            //not yet authenticated
            log.error("You are not yet authenticated");
            authenticateRcon();
            return CompletableFuture.failedFuture(e);
        }
    }

    private void authenticateRcon() {
        SourceServerInfo info = tvServerList.getSelectionModel().getSelectedItem();

        if (rconClient.isAuthenticated(info.getAddress())) {
            log.warn("Address {} is already authenticated", info.getAddress());
            return;
        }

        log.info("Authenticating address: {}", info.getAddress());

        TextInputDialog passDialog = new TextInputDialog();
        passDialog.setHeaderText("Enter rcon password");
        passDialog.setContentText("Password");
        Optional<String> password = passDialog.showAndWait();

        if (password.isPresent()) {
            rconClient.authenticate(info.getAddress(), password.get()).whenComplete((status, throwable) -> {
                if (throwable != null) {
                    log.error("Problem authenticating with server {}", info.getAddress());
                    return;
                }
                if (!status.isAuthenticated()) {
                    log.warn("RCON Authentication failed for server {} (Reason: {})", info.getAddress(), status.getReason());
                    return;
                }

                log.info("Successfully authenticated with server: {}", info.getAddress());

                String logCommand = "logaddress_add " + sourceLogListenService.getListenAddress().getAddress().getHostAddress() + ":" + sourceLogListenService.getListenAddress().getPort();

                //Start listening to logs
                executeRconCommand(logCommand).thenAccept(s -> {
                    try {
                        sourceLogListenService.setLogEventCallback(MainController.this::onLogReceive);
                        log.info("Attempting to listen to server logs on {}", sourceLogListenService.getListenAddress());
                        sourceLogListenService.listen();
                        log.info("Listening for server log events : {}", sourceLogListenService.getListenAddress());
                    } catch (InterruptedException e) {
                        log.error("Error during listen service initialization", e);
                    }
                });
            });
            log.info("Authenticating with server: {}", info.getAddress());
        }
    }

    private void onLogReceive(SourceLogEntry sourceLogEntry) {
        log.info("Got server log: {}", sourceLogEntry);
        taServerLog.appendText(sourceLogEntry.getMessage());
    }

    private void setupMasterServerToolbarComponents() {
        tfSearchCriteria.textProperty().addListener((observable, oldValue, newValue) -> {
            Predicate<SourceServerInfo> criteria = info -> {
                if (newValue == null || newValue.isBlank())
                    return true;
                String search = newValue.toLowerCase();
                return info.getName().toLowerCase().contains(search) || info.getMapName().toLowerCase().contains(search);
            };
            synchronized (lock) {
                filteredMasterServers.setPredicate(criteria);
            }
        });
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
        tvServerList.getSelectionModel().selectedItemProperty().addListener(this::processNewItemSelection);
        tvServerList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        tvServerList.getColumns().clear();
        //noinspection unchecked
        tvServerList.getColumns().addAll(nameCol, ipCol, playerCountCol, maxPlayerCountCol, mapNameCol, serverTagsCol);

        btnListServers.setOnAction(this::refreshMasterServerList);
    }

    private void processNewItemSelection(ObservableValue<? extends SourceServerInfo> observable, SourceServerInfo oldInfo, SourceServerInfo newInfo) {
        if (oldInfo != null && tvServerPlayerInfo.itemsProperty().isBound())
            tvServerPlayerInfo.itemsProperty().unbind();
        if (newInfo == null)
            return;

        //Check last update interval
        if ((System.currentTimeMillis() - newInfo.getLastUpdate()) >= 3000) {
            updateSourceServerInfo(newInfo).thenAccept(sourceServerInfo -> {
                populatePlayerDetailsTable(newInfo);
                populateServerDetailsTable(newInfo);
                populateServerRulesTable(newInfo);
            });
        } else {
            log.info("Source server still up to date: {}. Skipping update process", newInfo.getAddress());
        }
    }

    private CompletableFuture<SourceServerInfo> updateSourceServerInfo(SourceServerInfo info) {
        if (serverInfoFuture != null && !serverInfoFuture.isDone()) {
            serverInfoFuture.cancel(true);
        }
        serverInfoFuture = serverQueryClient.getServerInfo(info.getAddress()).thenApply(update -> {
            info.setName(update.getName());
            info.setPlayerCount(update.getNumOfPlayers());
            info.setMaxPlayerCount(update.getMaxPlayers());
            info.setMapName(update.getMapName());
            info.setServerTags(update.getServerTags());
            info.setDescription(update.getGameDescription());
            info.setLastUpdate(System.currentTimeMillis());
            return info;
        });
        return serverInfoFuture;
    }

    private void refreshMasterServerList(ActionEvent actionEvent) {
        if (masterServerListFuture != null && !masterServerListFuture.isDone()) {
            log.warn("Process still running");
            return;
        }

        log.info("Listing all servers: {}", serverQueryClient);
        synchronized (lock) {
            masterServerList.clear();
            tvServerList.setItems(filteredMasterServers);

            MasterServerFilter filter = MasterServerFilter.create().dedicated(true).appId(550);
            masterServerListFuture = masterQueryClient.getServerList(MasterServerType.SOURCE, MasterServerRegion.REGION_ALL, filter, this::populateMasterServerListItem)
                    .whenComplete((inetSocketAddresses, throwable) -> {
                        log.info("Done");
                        piMasterServer.setVisible(false);
                    });
            piMasterServer.setVisible(true);
        }
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
        serverQueryClient.getServerRules(info.getAddress()).thenAccept(rulesMap -> {
            info.setRules(FXCollections.observableMap(rulesMap));
            for (var entry : info.getRules().entrySet())
                addProperty(tvServerRules, entry.getKey(), entry.getValue());
        });
    }

    private void populatePlayerDetailsTable(final SourceServerInfo info) {
        serverQueryClient.getPlayers(info.getAddress()).thenAccept(sourcePlayers -> {
            ObservableList<SourcePlayerInfo> playerInfoList = sourcePlayers
                    .stream()
                    .map(SourcePlayerInfo::new)
                    .collect(Collectors.toCollection(FXCollections::observableArrayList));

            tvServerPlayerInfo.itemsProperty().bind(info.playersProperty());
            info.setPlayers(playerInfoList);
            tvServerPlayerInfo.requestLayout();
        });
    }

    private void populateMasterServerListItem(InetSocketAddress address, InetSocketAddress sender, Throwable throwable) {
        serverQueryClient.getServerInfo(address).thenAccept(details -> {
            synchronized (lock) {
                details.setAddress(address);
                masterServerList.add(new SourceServerInfo(details));
            }
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

    @Autowired
    public void setRconClient(SourceRconClient rconClient) {
        this.rconClient = rconClient;
    }

    @Autowired
    public void setSourceLogListenService(SourceLogListenService sourceLogListenService) {
        this.sourceLogListenService = sourceLogListenService;
    }

    @Autowired
    public void setPublicIp(InetAddress publicIp) {
        this.publicIp = publicIp;
    }
}
