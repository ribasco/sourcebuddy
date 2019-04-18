package com.ibasco.sourcebuddy.controllers;

import com.ibasco.sourcebuddy.components.TaskManager;
import com.ibasco.sourcebuddy.constants.Icons;
import com.ibasco.sourcebuddy.constants.Views;
import com.ibasco.sourcebuddy.domain.ConfigProfile;
import com.ibasco.sourcebuddy.domain.ManagedServer;
import com.ibasco.sourcebuddy.events.ApplicationInitEvent;
import com.ibasco.sourcebuddy.gui.skins.CustomTaskProgressViewSkin;
import com.ibasco.sourcebuddy.model.ServerDetailsModel;
import com.ibasco.sourcebuddy.service.ConfigService;
import com.ibasco.sourcebuddy.util.ResourceUtil;
import javafx.collections.ListChangeListener;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.MenuBar;
import javafx.scene.control.SplitPane;
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

import java.util.concurrent.CompletableFuture;

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
    private CheckComboBox cbDocks;
    //</editor-fold>

    private ServerDetailsModel serverDetailsModel;

    private PopOver serviceStatusPopOver;

    private TaskManager taskManager;

    private TaskProgressView<Task<?>> taskProgressView;

    private Button btnServiceStatus;

    @FXML
    private Button btnSave;

    private ConfigService configService;

    @Override
    public void initialize(Stage stage, Node rootNode) {
        setupDocks(stage);
        setupTaskProgressView();
        updateSplitPaneResizable(dpMainDock.getChildren(), 0);
        setupMainToolbar();
        setupServiceStatusPopOver();
        setupStatusBar();

        btnSave.setOnAction(event -> {
            ConfigProfile profile = configService.createProfile();
            ManagedServer managedServer = new ManagedServer();
            managedServer.setProfile(profile);
            managedServer.setServerDetails(serverDetailsModel.getSelectedServer());
            profile.getManagedServers().add(managedServer);
            configService.saveProfile(profile);
            configService.setDefaultProfile(profile);
            log.debug("Successfully saved profile: {}", profile);
        });

        publishEvent(new ApplicationInitEvent(this, stage));
    }

    private void setupTaskProgressView() {
        taskProgressView = new TaskProgressView<>();
        taskProgressView.setGraphicFactory(param -> ResourceUtil.loadIconView(Icons.UPDATE_ICON));
        DropShadow borderGlow = new DropShadow();
        borderGlow.setOffsetY(0f);
        borderGlow.setOffsetX(0f);
        borderGlow.setColor(Color.web("#53BE6B"));
        borderGlow.setWidth(40);
        borderGlow.setHeight(40);

        taskProgressView.setSkin(new CustomTaskProgressViewSkin<>(taskProgressView));

        taskProgressView.getTasks().addListener((ListChangeListener<Task<?>>) c -> {
            int size = taskProgressView.getTasks().size();
            if (size > 0) {
                btnServiceStatus.setText(String.format("Status [%d]", size));
                btnServiceStatus.setEffect(borderGlow);
            } else {
                btnServiceStatus.setText("Status");
                btnServiceStatus.setEffect(null);
            }
        });

        log.debug("setupTaskProgressView() :: Bindings content between TaskProgressView's task list and task manager's task list");
        taskManager.getTaskMap().addListener(this::handleTaskMapChangeEvents);
    }

    private void handleTaskMapChangeEvents(MapChangeListener.Change<? extends Task<?>, ? extends CompletableFuture<?>> change) {
        if (change.wasAdded()) {
            taskProgressView.getTasks().add(change.getKey());
            log.debug("Added new task on task list: {}", change.getKey());
        } else if (change.wasRemoved()) {
            taskProgressView.getTasks().remove(change.getKey());
            log.debug("Removed task from task list: {}", change.getKey());
        }
    }

    private void setupMainToolbar() {

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

        Pane serverBrowserPane = viewManager.loadView(Views.DOCK_SERVER_BROWSER);
        Pane playerBrowserPane = viewManager.loadView(Views.DOCK_PLAYER_BROWSER);
        Pane rulesBrowserPane = viewManager.loadView(Views.DOCK_RULES_BROWSER);
        Pane serverManagerPane = viewManager.loadView(Views.DOCK_SERVER_MANAGER);
        Pane logsPane = viewManager.loadView(Views.DOCK_LOGS);
        Pane serverChatPane = viewManager.loadView(Views.DOCK_SERVER_CHAT);
        //Pane gameBrowserPane = viewManager.loadView(Views.DOCK_GAME_BROWSER);

        dpMainDock.addEventHandler(DockEvent.DOCK_RELEASED, event -> updateSplitPaneResizable(dpMainDock.getChildren(), 0));

        DockNode serverBrowserDock = new DockNode(serverBrowserPane, "Servers", serverBrowserImage);
        serverBrowserDock.setDockTitleBar(null); //prevent from being un-docked
        serverBrowserDock.setPrefWidth(1300);
        serverBrowserDock.dock(dpMainDock, DockPos.TOP);

        DockNode playerBrowserDock = new DockNode(playerBrowserPane, "Players", playerBrowserImage);
        playerBrowserDock.setMinWidth(350);
        playerBrowserDock.setPrefWidth(350);
        playerBrowserDock.dock(dpMainDock, DockPos.RIGHT);

        DockNode rulesBrowserDock = new DockNode(rulesBrowserPane, "Server Rules", rulesBrowserImage);
        rulesBrowserDock.setMinWidth(350);
        rulesBrowserDock.setPrefWidth(350);
        rulesBrowserDock.dock(dpMainDock, DockPos.BOTTOM, playerBrowserDock);

        DockNode serverManagerDock = new DockNode(serverManagerPane, "Control Panel", serverManagerImage);
        serverManagerDock.dock(dpMainDock, DockPos.BOTTOM, serverBrowserDock);

        DockNode logsDock = new DockNode(logsPane, "Logs", logsImage);
        logsDock.dock(dpMainDock, DockPos.CENTER, serverManagerDock);

        DockNode serverChatDock = new DockNode(serverChatPane, "Server Chat", chatImage);
        serverChatDock.dock(dpMainDock, DockPos.CENTER, serverManagerDock);

        /*DockNode gameBrowserDock = new DockNode(gameBrowserPane, "Game Browser");
        gameBrowserDock.setId("dockGameBrowsr");
        gameBrowserDock.setPrefWidth(230);
        gameBrowserDock.setMinWidth(250);
        gameBrowserDock.dock(dpMainDock, DockPos.LEFT);*/
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
    public void setConfigService(ConfigService configService) {
        this.configService = configService;
    }
}
