package com.ibasco.sourcebuddy.controllers;

import com.ibasco.sourcebuddy.components.TaskManager;
import com.ibasco.sourcebuddy.constants.Icons;
import com.ibasco.sourcebuddy.constants.Views;
import com.ibasco.sourcebuddy.domain.SteamApp;
import com.ibasco.sourcebuddy.events.ApplicationInitEvent;
import com.ibasco.sourcebuddy.model.ServerDetailsModel;
import com.ibasco.sourcebuddy.service.SteamQueryService;
import com.ibasco.sourcebuddy.tasks.UpdateMasterServerListTask;
import com.ibasco.sourcebuddy.tasks.UpdateServerDetailsTask;
import static com.ibasco.sourcebuddy.util.GuiUtil.findNode;
import com.ibasco.sourcebuddy.util.ResourceUtil;
import javafx.beans.binding.Bindings;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.MenuBar;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextInputDialog;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.controlsfx.control.*;
import org.dockfx.DockEvent;
import org.dockfx.DockNode;
import org.dockfx.DockPane;
import org.dockfx.DockPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.Optional;

@Controller
public class MainController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(MainController.class);

    //<editor-fold desc="FXML Properties">
    @FXML
    private MenuBar mbMainMenu;

    @FXML
    private DockPane dpMainDock;

    @FXML
    private StatusBar sbMainStatus;

    @FXML
    private NotificationPane npMain;

    @FXML
    private Button btnUpdateServerDetails;

    @FXML
    private Button btnUpdateMasterList;

    @FXML
    private CheckComboBox cbDocks;
    //</editor-fold>

    private ServerDetailsModel serverDetailsModel;

    private PopOver serviceStatusPopOver;

    private TaskManager taskManager;

    private SteamQueryService steamQueryService;

    private TaskProgressView<Task<?>> taskProgressView;

    private Button btnServiceStatus;

    @Override
    public void initialize(Stage stage, Node rootNode) {

        setupDocks(stage);
        setupTaskProgressView();
        updateSplitPaneResizable(dpMainDock.getChildren(), 0);
        setupMainToolbar();
        setupServiceStatusPopOver();
        setupStatusBar();

        //Notify listeners
        publishEvent(new ApplicationInitEvent(this, stage));
    }

    private void setupTaskProgressView() {
        taskProgressView = new TaskProgressView<>();
        taskProgressView.setGraphicFactory(param -> ResourceUtil.loadIconView(Icons.UPDATE_ICON));
        DropShadow borderGlow = new DropShadow();
        borderGlow.setOffsetY(0f);
        borderGlow.setOffsetX(0f);
        borderGlow.setColor(Color.GREEN);
        borderGlow.setWidth(40);
        borderGlow.setHeight(40);

        taskProgressView.getTasks().addListener((ListChangeListener<Task<?>>) c -> {
            while (c.next()) {
                int added = c.getAddedSize();
                if (added > 0) {
                    //getNotificationManager().showError(npMain, "Task added");
                    btnServiceStatus.setEffect(borderGlow);
                    btnServiceStatus.setText(String.format("Status [%d]", added));
                } else {
                    btnServiceStatus.setText("Status");
                    btnServiceStatus.setEffect(null);
                }
            }
        });
        log.debug("setupTaskProgressView() :: Bindings content between TaskProgressView's task list and task manager's task list");
        Bindings.bindContent(taskProgressView.getTasks(), taskManager.getTaskList());
    }

    private void setupMainToolbar() {
        btnUpdateMasterList.setOnAction(event -> {
            //Optional<String> value = prompt("Enter steam id", "Enter steam id");
            //if (value.isPresent()) {
            //int appId = Integer.valueOf(value.get());

            int[] appIds = new int[] {440, 550, 730};
            for (int appId : appIds) {
                Optional<SteamApp> steamApp = steamQueryService.findSteamAppById(appId);
                if (steamApp.isEmpty())
                    continue;
                taskManager.executeTask(UpdateMasterServerListTask.class, steamApp);
            }
            //}
        });

        btnUpdateServerDetails.setOnAction(event -> {
            taskManager.executeTask(UpdateServerDetailsTask.class, serverDetailsModel.getServerDetails());
        });
    }

    private Optional<String> prompt(String header, String content) {
        TextInputDialog textInputDialog = new TextInputDialog();
        textInputDialog.setHeaderText(header);
        textInputDialog.setContentText(content);
        return textInputDialog.showAndWait();
    }

    private void setupStatusBar() {
        ImageView statusGraphic = ResourceUtil.loadIconView(Icons.SERVICE_STATUS_ICON, 24, 24);
        btnServiceStatus = new Button("Status");
        btnServiceStatus.setGraphic(statusGraphic);
        btnServiceStatus.setOnAction(event -> serviceStatusPopOver.show(btnServiceStatus));
        sbMainStatus.getRightItems().add(btnServiceStatus);
    }

    private void setupServiceStatusPopOver() {
        serviceStatusPopOver = new PopOver(taskProgressView);
        serviceStatusPopOver.setDetachable(true);
        serviceStatusPopOver.setDetached(false);
        serviceStatusPopOver.setTitle("Task Status");
        serviceStatusPopOver.setAnimated(true);
        serviceStatusPopOver.setArrowLocation(PopOver.ArrowLocation.BOTTOM_RIGHT);
    }

    private void setupDocks(Stage mainScene) {
        ImageView serverBrowserImage = ResourceUtil.loadIconView(Icons.SERVER_BROWSER_ICON);
        ImageView playerBrowserImage = ResourceUtil.loadIconView(Icons.PLAYER_BROWSER_ICON);
        ImageView rulesBrowserImage = ResourceUtil.loadIconView(Icons.RULES_BROWSER_ICON);
        ImageView serverManagerImage = ResourceUtil.loadIconView(Icons.SERVER_MANAGER_ICON);
        ImageView logsImage = ResourceUtil.loadIconView(Icons.LOGS_ICON);
        ImageView chatImage = ResourceUtil.loadIconView(Icons.CHAT_ICON);

        Pane serverBrowserPane = getViewManager().loadView(Views.DOCK_SERVER_BROWSER);
        Pane playerBrowserPane = getViewManager().loadView(Views.DOCK_PLAYER_BROWSER);
        Pane rulesBrowserPane = getViewManager().loadView(Views.DOCK_RULES_BROWSER);
        Pane serverManagerPane = getViewManager().loadView(Views.DOCK_SERVER_MANAGER);
        Pane logsPane = getViewManager().loadView(Views.DOCK_LOGS);
        Pane serverChatPane = getViewManager().loadView(Views.DOCK_SERVER_CHAT);
        Pane gameBrowserPane = getViewManager().loadView(Views.DOCK_GAME_BROWSER);

        dpMainDock.addEventHandler(DockEvent.DOCK_RELEASED, new EventHandler<DockEvent>() {
            @Override
            public void handle(DockEvent event) {
                DockNode dockNode = (DockNode) event.getContents();

                Parent content = (Parent) dockNode.getContents();
                //findNode(content.getChildrenUnmodifiable(), 0);
                //findNodeX(content, 0);
                //log.debug("Dock: Node: {}, Parent: {}, Parent UP: {}, Content: {}, Width: {}, Height: {}", dockNode, dockNode.getParent(), dockNode.getParent().getParent(), dockNode.getContents(), dockNode.getWidth(), dockNode.getHeight());
                MasterDetailPane n = findNode(content, MasterDetailPane.class);

                //dockNode.setPrefSize(100, 200);
                if (n != null) {

                    log.debug("Found {}, Last Dock Pos: {}", n, dockNode.getLastDockPos());
                }

            }
        });

        dpMainDock.addEventHandler(DockEvent.DOCK_RELEASED, event -> updateSplitPaneResizable(dpMainDock.getChildren(), 0));

        DockNode serverBrowserDock = new DockNode(serverBrowserPane, "Servers", serverBrowserImage);
        //serverBrowserDock.setDockTitleBar(null); //prevent from being un-docked
        serverBrowserDock.dock(dpMainDock, DockPos.TOP);

        DockNode playerBrowserDock = new DockNode(playerBrowserPane, "Players", playerBrowserImage);
        playerBrowserDock.dock(dpMainDock, DockPos.RIGHT);

        DockNode rulesBrowserDock = new DockNode(rulesBrowserPane, "Server Rules", rulesBrowserImage);
        rulesBrowserDock.dock(dpMainDock, DockPos.BOTTOM, playerBrowserDock);

        DockNode serverManagerDock = new DockNode(serverManagerPane, "Server Manager", serverManagerImage);
        serverManagerDock.dock(dpMainDock, DockPos.BOTTOM, serverBrowserDock);

        DockNode logsDock = new DockNode(logsPane, "Logs", logsImage);
        logsDock.dock(dpMainDock, DockPos.CENTER, serverManagerDock);

        DockNode serverChatDock = new DockNode(serverChatPane, "Server Chat", chatImage);
        serverChatDock.dock(dpMainDock, DockPos.CENTER, serverManagerDock);

        DockNode gameBrowserDock = new DockNode(gameBrowserPane, "Game Browser");
        gameBrowserDock.setId("dockGameBrowsr");
        gameBrowserDock.setPrefSize(100, 10);
        gameBrowserDock.dock(dpMainDock, DockPos.LEFT);
    }

    private void updateSplitPaneResizable(ObservableList<Node> items, int level) {
        for (var item : items) {
            //log.debug("{}{}) updateSplitPaneResizable() set -> {}", "\t".repeat(level), level, item);
            SplitPane.setResizableWithParent(item, false);
            if (item instanceof SplitPane) {
                updateSplitPaneResizable(((SplitPane) item).getItems(), level + 1);
            } else if (item instanceof DockNode) {
                updateSplitPaneResizable(((DockNode) item).getChildren(), level + 1);
            }
        }
    }

    @Autowired
    public void setServerDetailsModel(ServerDetailsModel serverDetailsModel) {
        this.serverDetailsModel = serverDetailsModel;
    }

    @Autowired
    public void setTaskManager(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @Autowired
    public void setSteamQueryService(SteamQueryService steamQueryService) {
        this.steamQueryService = steamQueryService;
    }
}
