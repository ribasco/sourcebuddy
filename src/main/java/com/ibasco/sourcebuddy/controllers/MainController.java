package com.ibasco.sourcebuddy.controllers;

import com.ibasco.sourcebuddy.constants.Icons;
import com.ibasco.sourcebuddy.constants.Views;
import com.ibasco.sourcebuddy.events.ApplicationInitEvent;
import com.ibasco.sourcebuddy.model.ServerDetailsModel;
import com.ibasco.sourcebuddy.service.ListenableTaskService;
import com.ibasco.sourcebuddy.service.ServerDetailsUpdateService;
import com.ibasco.sourcebuddy.util.ResourceUtil;
import javafx.collections.ListChangeListener;
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
import org.controlsfx.control.NotificationPane;
import org.controlsfx.control.PopOver;
import org.controlsfx.control.StatusBar;
import org.controlsfx.control.TaskProgressView;
import org.dockfx.DockNode;
import org.dockfx.DockPane;
import org.dockfx.DockPos;
import org.dockfx.DockTitleBar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

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
    //</editor-fold>

    private ServerDetailsModel serverDetailsModel;

    private ServerDetailsUpdateService serverDetailsUpdateService;

    private TaskProgressView<Task<?>> taskProgressView;

    private PopOver serviceStatusPopOver;

    @Override
    public void initialize(Stage stage, Node rootNode) {
        setupDocks();
        processDocks(dpMainDock.getChildren(), 0);

        log.info("View node for controller: {}", getViewManager().getViewName(this));

        setupTaskProgressView();
        setupServiceStatusPopOver();
        setupStatusBar();
        monitorService(serverDetailsUpdateService);

        //Notify listeners
        publishEvent(new ApplicationInitEvent(this, stage));
    }

    private void monitorService(ListenableTaskService<?> service) {
        log.debug("Monitoring service: {}", service.getClass().getSimpleName());
        service.setOnRunning(event -> taskProgressView.getTasks().add(service.getTask()));
    }

    private void setupStatusBar() {
        Button btnServiceStatus = new Button("Status");

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
                    getNotificationManager().showError(npMain, "Task added");
                    btnServiceStatus.setEffect(borderGlow);
                    btnServiceStatus.setText(String.format("Status [%d]", added));
                } else {
                    btnServiceStatus.setText("Status");
                    btnServiceStatus.setEffect(null);
                }
            }
        });
        ImageView statusGraphic = ResourceUtil.loadIconView(Icons.SERVICE_STATUS_ICON, 24, 24);
        btnServiceStatus.setGraphic(statusGraphic);
        btnServiceStatus.setOnAction(event -> serviceStatusPopOver.show(btnServiceStatus));
        sbMainStatus.getRightItems().add(btnServiceStatus);
    }

    private void setupTaskProgressView() {
        taskProgressView = new TaskProgressView<>();
        taskProgressView.setGraphicFactory(param -> ResourceUtil.loadIconView(Icons.UPDATE_ICON));
    }

    private void setupServiceStatusPopOver() {
        serviceStatusPopOver = new PopOver(taskProgressView);
        serviceStatusPopOver.setDetachable(true);
        serviceStatusPopOver.setDetached(false);
        serviceStatusPopOver.setTitle("Task Status");
        serviceStatusPopOver.setAnimated(true);
        serviceStatusPopOver.setArrowLocation(PopOver.ArrowLocation.BOTTOM_RIGHT);
    }

    private void setupDocks() {
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

        DockNode serverBrowserDock = new DockNode(serverBrowserPane, "Servers", serverBrowserImage);
        serverBrowserDock.setDockTitleBar(null); //prevent from being un-docked
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
        gameBrowserDock.setPrefSize(200, 200);
        gameBrowserDock.dock(dpMainDock, DockPos.LEFT);
    }

    private void processDocks(ObservableList<Node> items, int level) {
        for (var item : items) {
            String parent = item.getParent() == null ? "N/A" : item.getParent().getClass().getSimpleName();
            SplitPane.setResizableWithParent(item, false);
            if (item instanceof SplitPane) {
                log.debug("{}{} = Item: {}, Parent: {} (Instance: {}, Name: {})", "\t".repeat(level), level, item.getClass().getSimpleName(), parent, item.hashCode(), ((SplitPane) item).getOrientation().name());
            } else if (item instanceof DockNode) {
                DockNode node = (DockNode) item;
                DockTitleBar titleBar = node.getDockTitleBar();
                if (titleBar == null) {
                    log.debug("{}{} = Item: {}, Parent: {} (Instance: {}, Label: {})", "\t".repeat(level), level, item.getClass().getSimpleName(), parent, item.hashCode(), "N/A");
                } else {
                    log.debug("{}{} = Item: {}, Parent: {} (Instance: {}, Label: {})", "\t".repeat(level), level, item.getClass().getSimpleName(), parent, item.hashCode(), titleBar.getLabel().getText());
                }
            } else {
                log.debug("{}{} = Item: {}, Parent: {} (Instance: {})", "\t".repeat(level), level, item.getClass().getSimpleName(), parent, item.hashCode());
            }

            if (item instanceof SplitPane) {
                processDocks(((SplitPane) item).getItems(), level + 1);
            } else if (item instanceof DockNode) {
                processDocks(((DockNode) item).getChildren(), level + 1);
            }
        }
    }

    @Autowired
    public void setServerDetailsUpdateService(ServerDetailsUpdateService serverDetailsUpdateService) {
        this.serverDetailsUpdateService = serverDetailsUpdateService;
    }

    @Autowired
    public void setServerDetailsModel(ServerDetailsModel serverDetailsModel) {
        this.serverDetailsModel = serverDetailsModel;
    }
}
