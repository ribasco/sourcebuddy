package com.ibasco.sourcebuddy.controllers;

import com.ibasco.sourcebuddy.domain.ServerDetails;
import com.ibasco.sourcebuddy.model.ServerDetailsModel;
import com.ibasco.sourcebuddy.service.ServerDetailsUpdateService;
import static com.ibasco.sourcebuddy.util.GuiUtil.createBasicColumn;
import com.ibasco.sourcebuddy.util.SourceDetailsTableViewFactory;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import org.controlsfx.control.MasterDetailPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@SuppressWarnings("Duplicates")
@Controller
public class ServerBrowserController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(ServerBrowserController.class);

    //<editor-fold desc="FXML Properties">
    @FXML
    private MasterDetailPane mdpServerBrowser;

    @FXML
    private TableView<ServerDetails> serverListTable;
    //</editor-fold>

    private ServerDetailsModel serverDetailsModel;

    private ServerDetailsUpdateService serverDetailsUpdateService;

    private SourceDetailsTableViewFactory serverBrowserTableCellFactory;

    @Override
    public void initialize(Stage stage, Node rootNode) {
        log.info("Initializing server browser controller");
        log.info("Got: {}", mdpServerBrowser);

        setupServerBrowserTable();

        serverListTable.itemsProperty().bind(serverDetailsModel.serverDetailsProperty());
        serverDetailsModel.statusMessageProperty().bind(serverDetailsUpdateService.messageProperty());
        serverDetailsModel.serverSelectionModelProperty().bind(serverListTable.selectionModelProperty());
        serverDetailsUpdateService.start();
    }

    private void setupServerBrowserTable() {
        serverListTable.getColumns().clear();

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
    public void setServerDetailsUpdateService(ServerDetailsUpdateService serverDetailsUpdateService) {
        this.serverDetailsUpdateService = serverDetailsUpdateService;
    }

    @Autowired
    public void setServerDetailsModel(ServerDetailsModel serverDetailsModel) {
        this.serverDetailsModel = serverDetailsModel;
    }

    @Autowired
    public void setServerBrowserTableCellFactory(SourceDetailsTableViewFactory serverBrowserTableCellFactory) {
        this.serverBrowserTableCellFactory = serverBrowserTableCellFactory;
    }
}
