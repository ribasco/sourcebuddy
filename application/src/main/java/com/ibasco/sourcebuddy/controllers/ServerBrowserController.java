package com.ibasco.sourcebuddy.controllers;

import static com.ibasco.sourcebuddy.components.GuiHelper.*;
import com.ibasco.sourcebuddy.controls.ProgressComboBox;
import com.ibasco.sourcebuddy.domain.ConfigProfile;
import com.ibasco.sourcebuddy.domain.ServerDetails;
import com.ibasco.sourcebuddy.domain.SteamApp;
import com.ibasco.sourcebuddy.enums.ServerStatus;
import com.ibasco.sourcebuddy.gui.converters.BasicObjectStringConverter;
import com.ibasco.sourcebuddy.gui.tableview.factory.ServerBrowserTableViewFactory;
import com.ibasco.sourcebuddy.gui.tableview.rows.HighlightRow;
import com.ibasco.sourcebuddy.gui.treetableview.cells.FormattedTreeTableCell;
import com.ibasco.sourcebuddy.gui.treetableview.factory.BookmarksTreeTableCellFactory;
import com.ibasco.sourcebuddy.model.ServerDetailsModel;
import com.ibasco.sourcebuddy.model.SteamGamesModel;
import com.ibasco.sourcebuddy.model.TreeDataModel;
import com.ibasco.sourcebuddy.service.ServerManager;
import com.ibasco.sourcebuddy.service.SourceServerService;
import com.ibasco.sourcebuddy.service.impl.SingleServerDetailsRefreshService;
import com.ibasco.sourcebuddy.tasks.BuildBookmarkServerTreeTask;
import com.ibasco.sourcebuddy.tasks.BuildManagedServersTreeTask;
import com.ibasco.sourcebuddy.tasks.SwitchGameTask;
import com.ibasco.sourcebuddy.tasks.UpdateAllServerDetailsTask;
import com.jfoenix.controls.JFXProgressBar;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.MasterDetailPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.List;

@SuppressWarnings("Duplicates")
@Controller
public class ServerBrowserController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(ServerBrowserController.class);

    //<editor-fold desc="FXML Properties">
    @FXML
    private MasterDetailPane mdpServerBrowser;

    @FXML
    private TableView<ServerDetails> tvServerBrowser;

    @FXML
    private TreeTableView<ServerDetails> ttvBookmarkedServers;

    @FXML
    private TreeTableView<ServerDetails> ttvManagedServers;

    @FXML
    private ProgressComboBox<SteamApp> cbBookmarkedGames;

    @FXML
    private Button btnSetDefaultGame;

    @FXML
    private Button btnRefreshServerList;

    @FXML
    private JFXProgressBar pbServerLoad;

    @FXML
    private TabPane tpServers;

    @FXML
    private ToolBar tbServerPanel;

    @FXML
    private Tab tabBookmarks;

    @FXML
    private Tab tabManagedServers;

    @FXML
    private Tab tabServerBrowser;
    //</editor-fold>

    private ServerDetailsModel serverDetailsModel;

    private SteamGamesModel steamGamesModel;

    private BookmarksTreeTableCellFactory bookmarksTreeTableViewFactory;

    private ServerBrowserTableViewFactory serverBrowserTableCellFactory;

    private SourceServerService sourceServerQueryService;

    private FilteredList<ServerDetails> filteredServerDetailsList;

    private SingleServerDetailsRefreshService singleUpdateService;

    private ServerManager serverManager;

    @Override
    public void initialize(Stage stage, Node rootNode) {
        setupServerTabPanel();
        setupServerBrowserTable();
        setupBookmarksTable();
        setupManagedServersTable();
        setupGameSelectionBox();
        setupSingleServerUpdateService();

        hideDetailPaneOnHeightChange(mdpServerBrowser, 300);

        btnSetDefaultGame.setOnAction(this::setDefaultGame);
        tpServers.setTabDragPolicy(TabPane.TabDragPolicy.REORDER);
        pbServerLoad.visibleProperty().bind(serverDetailsModel.serverListUpdatingProperty());
        steamGamesModel.selectedGameProperty().addListener(this::updateSteamAppServerEntries);
        steamGamesModel.setSelectedGame(serverDetailsModel.getActiveProfile().getDefaultGame());
    }

    private void setDefaultGame(ActionEvent actionEvent) {
        SteamApp app = cbBookmarkedGames.getValue();

        ConfigProfile profile = serverDetailsModel.getActiveProfile();
        profile.setDefaultGame(app);
        getConfigService().saveProfile(profile);
    }

    private void setupSingleServerUpdateService() {
        singleUpdateService.serverDetailsProperty().bind(serverDetailsModel.selectedServerProperty());
        serverDetailsModel.selectedServerProperty().addListener(this::handleServerSelectionUpdate);
    }

    private void setupServerTabPanel() {
        tpServers.getSelectionModel().selectedItemProperty().addListener(this::handleTabSelectionChangeEvent);
    }

    private void handleTabSelectionChangeEvent(ObservableValue<? extends Tab> observableValue, Tab oldTab, Tab newTab) {
        serverDetailsModel.setSelectedServer(null);
        if (newTab == null) {
            return;
        }

        //Load items on tab selection
        if (newTab.equals(tabBookmarks)) {
            refreshBookmarksTable();
        } else if (newTab.equals(tabManagedServers)) {
            refreshManagedServersTable();
        }
    }

    private void updateSteamAppServerEntries(ObservableValue<? extends SteamApp> observableValue, SteamApp oldApp, SteamApp newApp) {
        if (newApp != null) {
            if (taskManager.isRunning(SwitchGameTask.class) || serverDetailsModel.isServerListUpdating()) {
                getNotificationManager().showWarning("Task is already running or Server list is currently getting populated");
                if (oldApp != null)
                    steamGamesModel.setSelectedGame(oldApp);
                return;
            }

            serverDetailsModel.setSelectedServer(null);
            serverDetailsModel.setServerListUpdating(true);
            tvServerBrowser.itemsProperty().unbind();
            serverDetailsModel.getServerDetails().clear();
            tvServerBrowser.setItems(null);

            Label placeHolder = new Label();
            placeHolder.setText(String.format("Updating server list for '%s'", newApp.getName()));
            tvServerBrowser.setPlaceholder(placeHolder);

            taskManager.run(SwitchGameTask.class, newApp)
                    .thenAccept(aVoid -> {
                        tvServerBrowser.itemsProperty().bind(serverDetailsModel.serverDetailsProperty());
                        serverDetailsModel.setServerListUpdating(false);
                    })
                    .thenCompose(aVoid -> {
                        serverDetailsModel.setServerDetailsUpdating(true);
                        return taskManager.run(UpdateAllServerDetailsTask.class, serverDetailsModel.getServerDetails());
                    })
                    .whenComplete((aVoid, throwable) -> {
                        try {
                            if (throwable != null) {
                                notificationManager.showError("Error occured during update %s", throwable.getMessage());
                                return;
                            }
                            getNotificationManager().showInfo("Completed updating server details");
                        } finally {
                            serverDetailsModel.setServerDetailsUpdating(false);
                            serverDetailsModel.setServerListUpdating(false);
                            Platform.runLater(() -> tvServerBrowser.setPlaceholder(null));
                        }
                    });
        }
    }

    private void setupGameSelectionBox() {
        cbBookmarkedGames.valueProperty().bindBidirectional(steamGamesModel.selectedGameProperty());
        cbBookmarkedGames.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                if (!serverDetailsModel.isServerListUpdating() && !serverDetailsModel.isServerDetailsUpdating()) {
                    steamGamesModel.setSelectedGame(newValue);
                } else {
                    log.debug("Server entries are still being updated");
                }
            }
        });

        cbBookmarkedGames.itemsProperty().bind(steamGamesModel.bookmarkedAppListProperty());
        cbBookmarkedGames.setConverter(new BasicObjectStringConverter<>());
        cbBookmarkedGames.showProgressProperty().bind(serverDetailsModel.serverListUpdatingProperty().or(serverDetailsModel.serverDetailsUpdatingProperty()));
    }

    private void refreshBookmarksTable() {
        ttvBookmarkedServers.setRoot(null);
        Parent placeholderView = getGuiHelper().getLoadingPlaceholder("Loading bookmarks...");
        ttvBookmarkedServers.setPlaceholder(placeholderView);

        taskManager.run(BuildBookmarkServerTreeTask.class)
                .thenCompose(r -> {
                    Platform.runLater(() -> {
                        ttvBookmarkedServers.setRoot(r);
                        ttvBookmarkedServers.setPlaceholder(null);
                    });

                    ObservableList<ServerDetails> serverList = FXCollections.observableArrayList();
                    log.debug("Building list from tree...");
                    buildListFromTree(r, serverList);

                    if (taskManager.isRunning(UpdateAllServerDetailsTask.class)) {
                        log.warn("Already running");
                        return null;
                    }

                    return taskManager.run(UpdateAllServerDetailsTask.class, serverList);
                });
    }

    private void refreshManagedServersTable() {
        log.debug("Refreshing managed servers table");
        ttvManagedServers.setRoot(null);
        Parent placeholderView = getGuiHelper().getLoadingPlaceholder("Loading managed servers...");
        ttvManagedServers.setPlaceholder(placeholderView);

        taskManager.run(BuildManagedServersTreeTask.class)
                .thenApply(this::convertToTreeItem)
                .thenCompose(r -> {
                    Platform.runLater(() -> {
                        ttvManagedServers.setRoot(r);
                        ttvManagedServers.setPlaceholder(null);
                    });

                    ObservableList<ServerDetails> serverList = FXCollections.observableArrayList();
                    buildListFromTree(r, serverList);

                    if (taskManager.isRunning(UpdateAllServerDetailsTask.class)) {
                        return null;
                    }

                    return taskManager.run(UpdateAllServerDetailsTask.class, serverList);
                });
    }

    private TreeItem<ServerDetails> convertToTreeItem(TreeDataModel<ServerDetails> data) {
        TreeItem<ServerDetails> root = new TreeItem<>();
        root.setExpanded(true);
        copyTreeDataToTreeItem(data, root);
        return root;
    }

    private void copyTreeDataToTreeItem(TreeDataModel<ServerDetails> source, TreeItem<ServerDetails> target) {
        for (TreeDataModel<ServerDetails> child : source.getChildren()) {
            if (child.getItem().getAddress() == null) {
                log.debug("Root: {}", child.getItem());
            } else {
                log.debug("\tItem: {}", child.getItem());
            }
            TreeItem<ServerDetails> childRoot = new TreeItem<>(child.getItem());
            childRoot.setExpanded(true);
            target.getChildren().add(childRoot);
            if (!child.getChildren().isEmpty()) {
                copyTreeDataToTreeItem(child, childRoot);
            }
        }
    }

    private void buildListFromTree(TreeItem<ServerDetails> root, List<ServerDetails> servers) {
        for (TreeItem<ServerDetails> child : root.getChildren()) {
            if (child.getValue() != null && child.getValue().getIpAddress() != null)
                servers.add(child.getValue());
            if (child.getChildren().size() > 0) {
                buildListFromTree(child, servers);
            }
        }
    }

    private void setupServerBrowserTable() {
        tvServerBrowser.getColumns().clear();
        tvServerBrowser.setRowFactory(param -> new HighlightRow<>(p -> ServerStatus.TIMED_OUT.equals(p.getStatus()), "timeout"));

        filteredServerDetailsList = serverDetailsModel.getServerDetails().filtered(p -> true);
        tvServerBrowser.setItems(filteredServerDetailsList);
        tvServerBrowser.setPlaceholder(new Label(""));

        Bindings.bindContent(serverDetailsModel.getSelectedServers(), tvServerBrowser.getSelectionModel().getSelectedItems());

        createBasicColumn(tvServerBrowser, "Bookmark", "bookmarked", serverBrowserTableCellFactory::drawBookmarkNode);
        createBasicColumn(tvServerBrowser, "Server Name", "name", serverBrowserTableCellFactory::drawServerName);
        createBasicColumn(tvServerBrowser, "IP Address", "ipAddress");
        createBasicColumn(tvServerBrowser, "Port", "port");
        createBasicColumn(tvServerBrowser, "Player Count", "playerCount");
        createBasicColumn(tvServerBrowser, "Max Players", "maxPlayerCount");
        createBasicColumn(tvServerBrowser, "Current Map", "mapName");
        createBasicColumn(tvServerBrowser, "Game", "steamApp", serverBrowserTableCellFactory::drawSteamApp);
        createBasicColumn(tvServerBrowser, "Status", "status", serverBrowserTableCellFactory::drawStatusInd);
        createBasicColumn(tvServerBrowser, "Country", "country", serverBrowserTableCellFactory::drawCountryIcon);
        createBasicColumn(tvServerBrowser, "OS", "operatingSystem", serverBrowserTableCellFactory::drawOperatingSystem);
        createBasicColumn(tvServerBrowser, "Update Date", "updateDate");
        createBasicColumn(tvServerBrowser, "Tags", "serverTags", serverBrowserTableCellFactory::drawTags).setPrefWidth(200);

        ContextMenu cMenu = new ContextMenu();

        MenuItem miManagedServer = new MenuItem();
        miManagedServer.textProperty().bind(Bindings.createStringBinding(() -> {
            ServerDetails selected = tvServerBrowser.getSelectionModel().getSelectedItem();
            int totalSelected = tvServerBrowser.getSelectionModel().getSelectedItems().size();
            if (serverManager.isManaged(selected)) {
                if (totalSelected > 1) {
                    return "Remove managed server(s)";
                }
                return "Remove managed server";
            }
            if (totalSelected > 1) {
                return "Add managed server(s)";
            }
            return "Add managed server";
        }, tvServerBrowser.getSelectionModel().selectedItemProperty()));
        miManagedServer.setOnAction(event -> {
            for (ServerDetails serverDetails : tvServerBrowser.getSelectionModel().getSelectedItems()) {
                if (!serverManager.isManaged(serverDetails)) {
                    log.debug("Adding managed server: {}", serverDetails);
                    serverManager.addServer(serverDetails);
                } else {
                    log.debug("Removing managed server: {}", serverDetails);
                    serverManager.removeServer(serverDetails);
                }
            }
        });

        cMenu.getItems().add(miManagedServer);
        tvServerBrowser.setContextMenu(cMenu);

        //Raise the following events on item selection
        tvServerBrowser.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        tvServerBrowser.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> serverDetailsModel.setSelectedServer(newValue));
    }

    private void handleServerSelectionUpdate(ObservableValue<? extends ServerDetails> observableValue, ServerDetails oldValue, ServerDetails newValue) {
        if (newValue == null) {
            singleUpdateService.cancel();
        } else {
            if (!singleUpdateService.isRunning()) {
                singleUpdateService.restart();
            }
        }
    }

    private void setupBookmarksTable() {
        ttvBookmarkedServers.getColumns().clear();
        ttvBookmarkedServers.setShowRoot(false);

        TreeTableColumn<ServerDetails, String> nameCol = createBasicTreeColumn(ttvBookmarkedServers, "Server Name", "name", bookmarksTreeTableViewFactory::serverName);
        createBasicTreeColumn(ttvBookmarkedServers, "IP Address", "ipAddress", bookmarksTreeTableViewFactory);
        createBasicTreeColumn(ttvBookmarkedServers, "Port", "port", bookmarksTreeTableViewFactory);
        createBasicTreeColumn(ttvBookmarkedServers, "Player Count", "playerCount", bookmarksTreeTableViewFactory);
        createBasicTreeColumn(ttvBookmarkedServers, "Max Players", "maxPlayerCount", bookmarksTreeTableViewFactory);
        createBasicTreeColumn(ttvBookmarkedServers, "Current Map", "mapName", bookmarksTreeTableViewFactory);
        createBasicTreeColumn(ttvBookmarkedServers, "Status", "status", bookmarksTreeTableViewFactory::statusIndicator);
        createBasicTreeColumn(ttvBookmarkedServers, "Country", "country", bookmarksTreeTableViewFactory::country);
        createBasicTreeColumn(ttvBookmarkedServers, "Tags", "serverTags", bookmarksTreeTableViewFactory::serverTags);
        //createBasicTreeColumn(ttvBookmarkedServers, "Game", "steamApp", bookmarksTreeTableViewFactory);

        ttvBookmarkedServers.setTreeColumn(nameCol);
        ttvBookmarkedServers.getSelectionModel().selectedItemProperty().addListener(this::updateServerSelection);
    }

    private void setupManagedServersTable() {
        ttvManagedServers.getColumns().clear();
        ttvManagedServers.setShowRoot(false);

        TreeTableColumn<ServerDetails, String> nameCol = createBasicTreeColumn(ttvManagedServers, "Server Name", "name", param -> new FormattedTreeTableCell<>("root-col", p -> StringUtils.isBlank(p.getIpAddress())));
        createBasicTreeColumn(ttvManagedServers, "IP Address", "ipAddress", bookmarksTreeTableViewFactory);
        createBasicTreeColumn(ttvManagedServers, "Port", "port", bookmarksTreeTableViewFactory);
        createBasicTreeColumn(ttvManagedServers, "Player Count", "playerCount", bookmarksTreeTableViewFactory);
        createBasicTreeColumn(ttvManagedServers, "Max Players", "maxPlayerCount", bookmarksTreeTableViewFactory);
        createBasicTreeColumn(ttvManagedServers, "Current Map", "mapName", bookmarksTreeTableViewFactory);
        createBasicTreeColumn(ttvManagedServers, "Game", "steamApp", bookmarksTreeTableViewFactory);
        createBasicTreeColumn(ttvManagedServers, "Status", "status", bookmarksTreeTableViewFactory::statusIndicator);
        createBasicTreeColumn(ttvManagedServers, "Country", "country", bookmarksTreeTableViewFactory::country);
        createBasicTreeColumn(ttvManagedServers, "Tags", "serverTags", bookmarksTreeTableViewFactory::serverTags);

        ttvManagedServers.setTreeColumn(nameCol);
        ttvManagedServers.getSelectionModel().selectedItemProperty().addListener(this::updateServerSelection);
    }

    private void updateServerSelection(ObservableValue<? extends TreeItem<ServerDetails>> observableValue, TreeItem<ServerDetails> oldValue, TreeItem<ServerDetails> newValue) {
        if (newValue != null) {
            serverDetailsModel.setSelectedServer(newValue.getValue());
        } else {
            serverDetailsModel.setSelectedServer(null);
        }
    }

    @Autowired
    public void setServerDetailsModel(ServerDetailsModel serverDetailsModel) {
        this.serverDetailsModel = serverDetailsModel;
    }

    @Autowired
    public void setServerBrowserTableCellFactory(ServerBrowserTableViewFactory serverBrowserTableCellFactory) {
        this.serverBrowserTableCellFactory = serverBrowserTableCellFactory;
    }

    @Autowired
    public void setSourceServerQueryService(SourceServerService sourceServerQueryService) {
        this.sourceServerQueryService = sourceServerQueryService;
    }

    @Autowired
    public void setSteamGamesModel(SteamGamesModel steamGamesModel) {
        this.steamGamesModel = steamGamesModel;
    }

    @Autowired
    public void setBookmarksTreeTableViewFactory(BookmarksTreeTableCellFactory bookmarksTreeTableViewFactory) {
        this.bookmarksTreeTableViewFactory = bookmarksTreeTableViewFactory;
    }

    @Autowired
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public void setSingleUpdateService(SingleServerDetailsRefreshService singleUpdateService) {
        this.singleUpdateService = singleUpdateService;
    }

    @Autowired
    public void setServerManager(ServerManager serverManager) {
        this.serverManager = serverManager;
    }
}
