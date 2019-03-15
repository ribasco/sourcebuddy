package com.ibasco.sourcebuddy.controllers;

import com.ibasco.agql.protocols.valve.source.query.client.SourceQueryClient;
import com.ibasco.agql.protocols.valve.source.query.client.SourceRconClient;
import com.ibasco.agql.protocols.valve.source.query.exceptions.RconNotYetAuthException;
import com.ibasco.agql.protocols.valve.source.query.logger.SourceLogEntry;
import com.ibasco.agql.protocols.valve.source.query.logger.SourceLogListenService;
import com.ibasco.agql.protocols.valve.steam.master.MasterServerFilter;
import com.ibasco.agql.protocols.valve.steam.master.client.MasterServerQueryClient;
import com.ibasco.agql.protocols.valve.steam.master.enums.MasterServerRegion;
import com.ibasco.agql.protocols.valve.steam.master.enums.MasterServerType;
import com.ibasco.sourcebuddy.dao.SourceServerDetailsDao;
import com.ibasco.sourcebuddy.model.SourceKeyValueInfo;
import com.ibasco.sourcebuddy.model.SourcePlayerInfo;
import com.ibasco.sourcebuddy.model.SourceServerDetails;
import com.ibasco.sourcebuddy.service.MasterServerQueryService;
import com.ibasco.sourcebuddy.service.ServerDetailsUpdateService;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.controlsfx.control.StatusBar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

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
    private TableView<SourceServerDetails> tvServerList;

    @FXML
    private TextField tfSearchCriteria;

    @FXML
    private ProgressIndicator piMasterServer;

    @FXML
    private StatusBar sbMain;
    //endregion

    private SourceQueryClient serverQueryClient;

    private MasterServerQueryClient masterQueryClient;

    private SourceRconClient rconClient;

    private SourceLogListenService sourceLogListenService;

    private ObservableList<SourceServerDetails> masterServerList = FXCollections.observableArrayList();

    private FilteredList<SourceServerDetails> filteredMasterServers = masterServerList.filtered(p -> true);

    private CompletableFuture<SourceServerDetails> serverInfoFuture = null;

    private CompletableFuture<Vector<InetSocketAddress>> masterServerListFuture = null;

    private LinkedList<String> rconHistory = new LinkedList<>();

    private final Object lock = new Object();

    private final Object logLock = new Object();

    private SourceServerDetailsDao sourceServerDao;

    private ServerDetailsUpdateService serverInfoUpdateService;

    private MasterServerQueryService masterServerQueryService;

    @Autowired
    public void setSourceServerDao(SourceServerDetailsDao sourceServerDao) {
        this.sourceServerDao = sourceServerDao;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        log.info("Main controller initialized");
        setupServerBrowserTable();
        setupMasterServerToolbarComponents();
        setupPlayerInfoTable();
        setupKeyValueTable(tvServerDetails);
        setupKeyValueTable(tvServerRules);
        setupMasterServerContextMenu();

        tfRcon.addEventHandler(KeyEvent.KEY_RELEASED, event -> {
            if (event.getCode() == KeyCode.ENTER) {
                SourceServerDetails info = tvServerList.getSelectionModel().getSelectedItem();
                if (info != null) {
                    String command = tfRcon.getText();
                    if (command == null || command.isBlank()) {
                        log.warn("No command entered");
                        return;
                    }
                    executeRconCommand(command).thenAccept(s -> tfRcon.setText(""));
                }
            }
            if (event.getCode() == KeyCode.UP) {
                log.info("Retrieving history");
                String rconCommand = rconHistory.pollFirst();
                if (rconCommand != null && !rconCommand.isBlank()) {
                    tfRcon.setText(rconCommand);
                } else {
                    tfRcon.clear();
                }
            }
        });

        serverInfoUpdateService.serverListProperty().bind(tvServerList.itemsProperty());

        refreshMasterServerList(null);
        setupStatusBar();

        serverInfoUpdateService.start();
        log.info("Started server info update service");
    }

    private void setupStatusBar() {
        sbMain.textProperty().bind(Bindings.format("Progress: %.2f%% (%.0f / %.0f), Duration: %s",
                serverInfoUpdateService.progressProperty().multiply(100),
                serverInfoUpdateService.workDoneProperty(),
                serverInfoUpdateService.totalWorkProperty(),
                Bindings.createStringBinding(() -> {
                    if (serverInfoUpdateService.getDuration() == null) {
                        return "N/A";
                    }
                    return serverInfoUpdateService.getDuration().toSeconds() + " seconds";
                }, serverInfoUpdateService.durationProperty())
        ));
        sbMain.progressProperty().bind(serverInfoUpdateService.progressProperty());
        /*Label duration = new Label();
        duration.setAlignment(Pos.CENTER_LEFT);
        duration.setTextAlignment(TextAlignment.CENTER);
        duration.setText("Duration: ");
        sbMain.getLeftItems().add(duration);*/
    }

    private void setupMasterServerContextMenu() {
        tvServerList.setRowFactory(param -> {
            final TableRow<SourceServerDetails> row = new TableRow<>();
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
        SourceServerDetails selectedServer = tvServerList.getSelectionModel().getSelectedItem();
        if (selectedServer == null)
            return CompletableFuture.failedFuture(new IllegalStateException("No server selected"));

        try {
            return rconClient.execute(selectedServer.getAddress(), command).thenApply(s -> {
                Platform.runLater(() -> {
                    synchronized (logLock) {
                        taServerLog.appendText(s);
                    }
                });
                rconHistory.push(command);
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
        SourceServerDetails info = tvServerList.getSelectionModel().getSelectedItem();

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
        Platform.runLater(() -> {
            synchronized (logLock) {
                taServerLog.appendText(sourceLogEntry.getMessage() + "\n");
            }
        });
    }

    private void setupMasterServerToolbarComponents() {
        tfSearchCriteria.textProperty().addListener((observable, oldValue, newValue) -> {
            Predicate<SourceServerDetails> criteria = info -> {
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
        tvServerList.getSelectionModel().selectedItemProperty().addListener(this::processNewItemSelection);
        tvServerList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        tvServerList.getColumns().clear();
        //noinspection unchecked
        tvServerList.getColumns().addAll(nameCol, ipCol, playerCountCol, maxPlayerCountCol, mapNameCol, statusCol, appIdCol, serverTagsCol, lastUpdateCol);

        btnListServers.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                log.info("Saving");
                //sourceServerDao.saveAll(masterServerList);
            }
        }); //this::refreshMasterServerList
    }

    private void processNewItemSelection(ObservableValue<? extends SourceServerDetails> observable, SourceServerDetails oldInfo, SourceServerDetails newInfo) {
        if (oldInfo != null && tvServerPlayerInfo.itemsProperty().isBound())
            tvServerPlayerInfo.itemsProperty().unbind();
        if (newInfo == null)
            return;

        //tvServerPlayerInfo.itemsProperty().bind(newInfo.playersProperty());

        //Check last update interval
        /*if ((System.currentTimeMillis() - newInfo.getLastUpdate()) >= 5000) {
            updateSourceServerInfo(newInfo).thenAccept(sourceServerInfo -> {
                populatePlayerDetailsTable(newInfo);
                populateServerDetailsTable(newInfo);
                populateServerRulesTable(newInfo);
            });
        } else {
            log.info("Source server still up to date: {}. Skipping update process", newInfo.getAddress());

            if (newInfo.getPlayers() != null && newInfo.getPlayers().size() > 0) {
                log.info("Players: {}", newInfo.getPlayers().size());
                tvServerPlayerInfo.itemsProperty().bind(newInfo.playersProperty());
                tvServerPlayerInfo.setItems(newInfo.getPlayers());
            }

            populateServerDetailsTable(newInfo);

            if (newInfo.getRules() != null && newInfo.getRules().size() > 0) {
                tvServerRules.getItems().clear();
                for (var entry : newInfo.getRules().entrySet())
                    addProperty(tvServerRules, entry.getKey(), entry.getValue());
            }
        }*/
    }

    private CompletableFuture<SourceServerDetails> updateSourceServerInfo(SourceServerDetails info) {
        /*if (serverInfoFuture != null && !serverInfoFuture.isDone()) {
            serverInfoFuture.cancel(false);
        }*/
        serverInfoFuture = serverQueryClient.getServerInfo(info.getAddress()).thenApply(update -> {
            info.setName(update.getName());
            info.setPlayerCount(update.getNumOfPlayers());
            info.setMaxPlayerCount(update.getMaxPlayers());
            info.setMapName(update.getMapName());
            info.setServerTags(update.getServerTags());
            info.setDescription(update.getGameDescription());
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

            List<SourceServerDetails> servers = sourceServerDao.findAll();

            if (servers.size() > 0) {
                //updateSourceServerInfo(s);
                masterServerList.addAll(servers);
                log.info("Cached server entries: {}", masterServerList.size());
            } else {
                MasterServerFilter filter = MasterServerFilter.create().dedicated(true).appId(550);
                masterServerListFuture = masterQueryClient.getServerList(MasterServerType.SOURCE, MasterServerRegion.REGION_ALL, filter, this::populateMasterServerListItem)
                        .whenComplete((inetSocketAddresses, throwable) -> {
                            log.info("Done");
                            piMasterServer.setVisible(false);
                            saveMasterServerList(masterServerList);
                        });
                piMasterServer.setVisible(true);
            }
        }
    }

    private void saveMasterServerList(List<SourceServerDetails> serverList) {
        log.info("Saving master list (Total: {})", serverList.size());
        serverList.forEach(info -> {
            try {
                log.info("\t- {} = {}:{}", info.getName(), info.getIpAddress(), info.getPort());
                sourceServerDao.save(info);
            } catch (Throwable e) {
                log.error("Error during save", e);
            }
        });
        log.info("Save complete (Total items: {})", serverList.size());
    }

    private void populateServerDetailsTable(final SourceServerDetails info) {
        Platform.runLater(() -> {
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
        });
    }

    private void populateServerRulesTable(final SourceServerDetails info) {
        tvServerRules.getItems().clear();
        serverQueryClient.getServerRules(info.getAddress()).thenAccept(rulesMap -> {
            Platform.runLater(() -> {
                /*info.setRules(FXCollections.observableMap(rulesMap));
                for (var entry : info.getRules().entrySet())
                    addProperty(tvServerRules, entry.getKey(), entry.getValue());*/
            });
        });
    }

    private void populatePlayerDetailsTable(final SourceServerDetails info) {
        serverQueryClient.getPlayersCached(info.getAddress()).thenAccept(sourcePlayers -> {
            ObservableList<SourcePlayerInfo> playerInfoList = sourcePlayers
                    .stream()
                    .map(SourcePlayerInfo::new)
                    .collect(Collectors.toCollection(FXCollections::observableArrayList));
            Platform.runLater(() -> {
                log.debug("Populating player info table: {}", info.getAddress());
                //tvServerPlayerInfo.itemsProperty().bind(info.playersProperty());
                //info.setPlayers(playerInfoList);
            });
        });
    }

    private void populateMasterServerListItem(InetSocketAddress address, InetSocketAddress sender, Throwable throwable) {
        serverQueryClient.getServerInfo(address).thenAccept(details -> {
            synchronized (lock) {
                details.setAddress(address);
                //add to master list
                masterServerList.add(new SourceServerDetails(details));
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
    public void setServerInfoUpdateService(ServerDetailsUpdateService serverInfoUpdateService) {
        this.serverInfoUpdateService = serverInfoUpdateService;
    }

    @Autowired
    public void setMasterServerQueryService(MasterServerQueryService masterServerQueryService) {
        this.masterServerQueryService = masterServerQueryService;
    }
}
