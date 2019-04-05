package com.ibasco.sourcebuddy.controllers;

import static com.ibasco.sourcebuddy.components.GuiHelper.createBasicColumn;
import static com.ibasco.sourcebuddy.components.GuiHelper.hideDetailPaneOnHeightChange;
import com.ibasco.sourcebuddy.controls.ProgressComboBox;
import com.ibasco.sourcebuddy.domain.ServerDetails;
import com.ibasco.sourcebuddy.domain.SteamApp;
import com.ibasco.sourcebuddy.enums.ServerStatus;
import com.ibasco.sourcebuddy.gui.converters.BasicObjectStringConverter;
import com.ibasco.sourcebuddy.gui.tableview.rows.HighlightRow;
import com.ibasco.sourcebuddy.model.ServerDetailsModel;
import com.ibasco.sourcebuddy.model.SteamGamesModel;
import com.ibasco.sourcebuddy.service.SourceServerService;
import com.ibasco.sourcebuddy.tasks.SwitchGameTask;
import com.ibasco.sourcebuddy.tasks.UpdateServerDetailsTask;
import com.ibasco.sourcebuddy.util.factory.ServerBrowserTableViewFactory;
import com.jfoenix.controls.JFXProgressBar;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.stage.Stage;
import org.controlsfx.control.MasterDetailPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.concurrent.CompletableFuture;

@SuppressWarnings("Duplicates")
@Controller
public class ServerBrowserController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(ServerBrowserController.class);

    //<editor-fold desc="FXML Properties">
    @FXML
    private MasterDetailPane mdpServerBrowser;

    @FXML
    private TableView<ServerDetails> serverListTable;

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
    //</editor-fold>

    private ServerDetailsModel serverDetailsModel;

    private SteamGamesModel steamGamesModel;

    private ServerBrowserTableViewFactory serverBrowserTableCellFactory;

    private SourceServerService sourceServerQueryService;

    @FXML
    private TabPane tpServers;

    @FXML
    private ProgressIndicator piServerRefresh;

    @FXML
    private ToolBar tbServerPanel;

    @Override
    public void initialize(Stage stage, Node rootNode) {
        setupServerTabPanel();
        setupServerBrowserTable();
        setupBookmarksTable();
        setupGameSelectionBox();
        hideDetailPaneOnHeightChange(mdpServerBrowser, 300);

        tpServers.setTabDragPolicy(TabPane.TabDragPolicy.REORDER);
        serverListTable.itemsProperty().bind(serverDetailsModel.serverDetailsProperty());
        serverDetailsModel.serverSelectionModelProperty().bind(serverListTable.selectionModelProperty());
        pbServerLoad.visibleProperty().bind(serverDetailsModel.serverListUpdatingProperty());
        serverListTable.setPlaceholder(new Label(""));
        steamGamesModel.selectedGameProperty().addListener(this::updateSteamAppServerEntries);
    }

    private void setupServerTabPanel() {
        showServerListProgressInd(false);
        Bindings.or(serverDetailsModel.serverListUpdatingProperty(), serverDetailsModel.serverListUpdatingProperty()).addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue)
                    log.debug("SERVER LIST/DETAILS ARE UPDATING");
            }
        });
    }

    private void showServerListProgressInd(boolean value) {
        if (value) {
            if (!tbServerPanel.getItems().contains(piServerRefresh)) {
                tbServerPanel.getItems().add(piServerRefresh);
            }
        } else {
            tbServerPanel.getItems().remove(piServerRefresh);
        }
    }

    private void updateSteamAppServerEntries(ObservableValue<? extends SteamApp> observableValue, SteamApp oldApp, SteamApp newApp) {
        if (newApp != null) {
            if (serverDetailsModel.isServerListUpdating()) {
                log.warn("Server list is currently getting populated");
                return;
            }

            serverDetailsModel.setServerListUpdating(true);
            serverListTable.itemsProperty().unbind();
            serverDetailsModel.getServerDetails().clear();
            serverListTable.setItems(null);

            Label placeHolder = new Label();
            placeHolder.setText(String.format("Updating server list for '%s'", newApp.getName()));
            serverListTable.setPlaceholder(placeHolder);

            CompletableFuture<Void> cf = taskManager.runTask(SwitchGameTask.class, newApp).whenComplete((aVoid, ex) -> {
                log.debug("SwitchGameTask() :: COMPLETED");
                serverListTable.itemsProperty().bind(serverDetailsModel.serverDetailsProperty());
                Platform.runLater(() -> serverListTable.setPlaceholder(null));
                serverDetailsModel.setServerListUpdating(false);
            }).thenCompose(a -> {
                log.debug("Updating server details");
                serverDetailsModel.setServerDetailsUpdating(true);
                return taskManager.runTask(UpdateServerDetailsTask.class, serverDetailsModel.getServerDetails());
            }).whenComplete((aVoid, throwable) -> {
                log.debug("DONE!!!!!!!!!!");
                serverDetailsModel.setServerDetailsUpdating(false);
            });
        }
    }

    private void setupGameSelectionBox() {
        cbBookmarkedGames.valueProperty().bindBidirectional(steamGamesModel.selectedGameProperty());
        cbBookmarkedGames.itemsProperty().bind(steamGamesModel.bookmarkedAppListProperty());
        cbBookmarkedGames.setConverter(new BasicObjectStringConverter<>());
        cbBookmarkedGames.showProgressProperty().bind(serverDetailsModel.serverListUpdatingProperty().or(serverDetailsModel.serverDetailsUpdatingProperty()));
        cbBookmarkedGames.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<SteamApp>() {
            @Override
            public void changed(ObservableValue<? extends SteamApp> observable, SteamApp oldValue, SteamApp newValue) {
                if (newValue != null) {
                    log.debug("You selected: {}", newValue);
                }
            }
        });
    }

    private void setupBookmarksTable() {
        ttvBookmarkedServers.getColumns().clear();

        TreeItem<ServerDetails> rootItem = new TreeItem<>();
        rootItem.setExpanded(true);

        createBasicTreeColumn(ttvBookmarkedServers, "Server Name", "name");
        createBasicTreeColumn(ttvBookmarkedServers, "IP Address", "ipAddress");
        createBasicTreeColumn(ttvBookmarkedServers, "Port", "port");
        createBasicTreeColumn(ttvBookmarkedServers, "Player Count", "playerCount");
        createBasicTreeColumn(ttvBookmarkedServers, "Max Players", "maxPlayerCount");
        createBasicTreeColumn(ttvBookmarkedServers, "Current Map", "mapName");
        createBasicTreeColumn(ttvBookmarkedServers, "Tags", "serverTags");
        createBasicTreeColumn(ttvBookmarkedServers, "Game", "steamApp");
        createBasicTreeColumn(ttvBookmarkedServers, "Status", "status");

        //Defining cell content
        ttvBookmarkedServers.setRoot(rootItem);
    }

    private <S, T> TreeTableColumn<S, T> createBasicTreeColumn(TreeTableView<S> treeTableView, String label, String propertyName) {
        return createBasicTreeColumn(treeTableView, label, propertyName, null);
    }

    private <S, T> TreeTableColumn<S, T> createBasicTreeColumn(TreeTableView<S> treeTableView, String label, String propertyName, ObservableValue<T> defaultProperty) {
        TreeTableColumn<S, T> column = new TreeTableColumn<>(label);
        treeTableView.getColumns().add(column);
        column.setCellValueFactory(param -> {
            TreeItemPropertyValueFactory<S, T> factory = new TreeItemPropertyValueFactory<>(propertyName);
            TreeItem<S> item = param.getValue();
            if (item.getValue() != null) {
                return factory.call(param);
            }
            return defaultProperty;
        });
        return column;
    }

    private void setupServerBrowserTable() {
        serverListTable.getColumns().clear();
        serverListTable.setRowFactory(param -> new HighlightRow<>(p -> ServerStatus.TIMED_OUT.equals(p.getStatus()), "highlight-timeout"));
        createBasicColumn(serverListTable, "Bookmark", "bookmarked", serverBrowserTableCellFactory::drawBookmarkNode);
        createBasicColumn(serverListTable, "Server Name", "name", serverBrowserTableCellFactory::drawServerName);
        createBasicColumn(serverListTable, "IP Address", "ipAddress"); //serverBrowserTableCellFactory::drawIpAddress
        createBasicColumn(serverListTable, "Port", "port");
        createBasicColumn(serverListTable, "Player Count", "playerCount");
        createBasicColumn(serverListTable, "Max Players", "maxPlayerCount");
        createBasicColumn(serverListTable, "Current Map", "mapName");
        createBasicColumn(serverListTable, "Tags", "serverTags", serverBrowserTableCellFactory::drawTags).setPrefWidth(200);
        createBasicColumn(serverListTable, "Game", "steamApp", serverBrowserTableCellFactory::drawSteamApp);
        createBasicColumn(serverListTable, "Status", "status", serverBrowserTableCellFactory::drawStatusInd);
        createBasicColumn(serverListTable, "Country", "country", serverBrowserTableCellFactory::drawCountryIcon);
        createBasicColumn(serverListTable, "Update Date", "updateDate");
        //Raise the following events on item selection
        serverListTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
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
}
