package com.ibasco.sourcebuddy.controllers;

import com.ibasco.sourcebuddy.domain.ServerDetails;
import com.ibasco.sourcebuddy.domain.SteamApp;
import com.ibasco.sourcebuddy.model.ServerDetailsModel;
import com.ibasco.sourcebuddy.service.SourceServerService;
import static com.ibasco.sourcebuddy.util.GuiUtil.createBasicColumn;
import static com.ibasco.sourcebuddy.util.GuiUtil.hideDetailPaneOnHeightChange;
import com.ibasco.sourcebuddy.util.SourceDetailsTableViewFactory;
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

import java.util.List;

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
    //</editor-fold>

    private ServerDetailsModel serverDetailsModel;

    private SourceDetailsTableViewFactory serverBrowserTableCellFactory;

    private SourceServerService sourceServerQueryService;

    @Override
    public void initialize(Stage stage, Node rootNode) {
        setupServerBrowserTable();
        setupBookmarksTable();
        hideDetailPaneOnHeightChange(mdpServerBrowser, 300);

        serverListTable.itemsProperty().bind(serverDetailsModel.serverDetailsProperty());
        serverDetailsModel.serverSelectionModelProperty().bind(serverListTable.selectionModelProperty());
    }

    private void setupBookmarksTable() {
        ttvBookmarkedServers.getColumns().clear();

        List<SteamApp> steamApps = sourceServerQueryService.findBookmarkedSteamApps();

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

        /*log.debug("Setting up columns");
        for (SteamApp app : steamApps) {
            for (ServerDetails server : sourceServerQueryService.findBookmarks(app)) {
                TreeItem<ServerDetails> item = new TreeItem<>(server);
                rootItem.getChildren().add(item);
            }
        }*/

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
        createBasicColumn(serverListTable, "Bookmark", "bookmarked", serverBrowserTableCellFactory::drawBookmarkNode);
        createBasicColumn(serverListTable, "Server Name", "name");
        createBasicColumn(serverListTable, "IP/Port", "address", serverBrowserTableCellFactory::drawIpAddress);
        createBasicColumn(serverListTable, "Player Count", "playerCount");
        createBasicColumn(serverListTable, "Max Players", "maxPlayerCount");
        createBasicColumn(serverListTable, "Current Map", "mapName");
        createBasicColumn(serverListTable, "Tags", "serverTags");
        createBasicColumn(serverListTable, "App ID", "steamApp", serverBrowserTableCellFactory::drawSteamApp);
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
    public void setServerBrowserTableCellFactory(SourceDetailsTableViewFactory serverBrowserTableCellFactory) {
        this.serverBrowserTableCellFactory = serverBrowserTableCellFactory;
    }

    @Autowired
    public void setSourceServerQueryService(SourceServerService sourceServerQueryService) {
        this.sourceServerQueryService = sourceServerQueryService;
    }
}
