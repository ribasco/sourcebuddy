package com.ibasco.sourcebuddy.controllers;

import com.ibasco.sourcebuddy.constants.Icons;
import com.ibasco.sourcebuddy.constants.Views;
import com.ibasco.sourcebuddy.model.ServerDetailsModel;
import com.ibasco.sourcebuddy.util.ResourceUtil;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.controlsfx.control.NotificationPane;
import org.controlsfx.control.PopOver;
import org.controlsfx.control.StatusBar;
import org.controlsfx.control.TaskProgressView;
import org.dockfx.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.Random;

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

    @Override
    public void initialize(Stage stage, Node rootNode) {
        setupDocks();
        recurse(dpMainDock.getChildren(), 0);
        sbMainStatus.textProperty().bind(serverDetailsModel.statusMessageProperty());
        Button btn = new Button("Check status");
        sbMainStatus.getRightItems().add(btn);

        VBox content = new VBox();
        TaskProgressView<Task<?>> taskProgressView = new TaskProgressView<>();
        content.getChildren().add(new Button("Test"));

        //Popover ex

        //Create PopOver and add look and feel
        PopOver popOver = new PopOver(taskProgressView);
        popOver.setDetachable(false);
        popOver.setDetached(false);
        popOver.setTitle("Test pop");
        popOver.setAnimated(true);
        popOver.setArrowLocation(PopOver.ArrowLocation.BOTTOM_RIGHT);

        log.info("View node for controller: {}", getViewManager().getViewName(this));

        btn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                popOver.show(btn);
            }
        });
    }

    private void setupDocks() {
        ImageView serverBrowserImage = ResourceUtil.loadIconView(Icons.SERVER_BROWSER_ICON);
        ImageView playerBrowserImage = ResourceUtil.loadIconView(Icons.PLAYER_BROWSER_ICON);
        ImageView rulesBrowserImage = ResourceUtil.loadIconView(Icons.RULES_BROWSER_ICON);
        ImageView serverManagerImage = ResourceUtil.loadIconView(Icons.SERVER_MANAGER_ICON);
        ImageView logsImage = ResourceUtil.loadIconView(Icons.LOGS_ICON);

        Pane serverBrowserPane = getViewManager().loadView(Views.DOCK_SERVER_BROWSER);
        Pane playerBrowserPane = getViewManager().loadView(Views.DOCK_PLAYER_BROWSER);
        Pane rulesBrowserPane = getViewManager().loadView(Views.DOCK_RULES_BROWSER);
        Pane serverManagerPane = getViewManager().loadView(Views.DOCK_SERVER_MANAGER);
        Pane logsPane = getViewManager().loadView(Views.DOCK_LOGS);

        setupListener(serverBrowserPane, playerBrowserPane, rulesBrowserPane, serverManagerPane);

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
        logsDock.dock(dpMainDock, DockPos.RIGHT, serverManagerDock);

        dpMainDock.addEventHandler(DockEvent.DOCK_ENTER, event -> log.info("Dock entered: {}", event));
        dpMainDock.addEventHandler(DockEvent.DOCK_EXIT, event -> log.info("Dock exited: {}", event));
        //dpMainDock.addEventHandler(DockEvent.DOCK_OVER, event -> log.info("Dock over: {}", event));
        dpMainDock.addEventHandler(DockEvent.DOCK_RELEASED, event -> log.info("Dock released: {}", event));
    }

    private void setupListener(Region... nodes) {
        for (Region node : nodes) {
            node.widthProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    log.info("[{}] Width: {}", node.getClass().getSimpleName(), newValue);
                }
            });
        }
    }

    private void recurse(ObservableList<Node> items, int level) {
        for (var item : items) {
            String parent = item.getParent() == null ? "N/A" : item.getParent().getClass().getSimpleName();
            SplitPane.setResizableWithParent(item, false);
            if (item instanceof SplitPane) {
                log.info("{}{} = Item: {}, Parent: {} (Instance: {}, Name: {})", "\t".repeat(level), level, item.getClass().getSimpleName(), parent, item.hashCode(), ((SplitPane) item).getOrientation().name());
            } else if (item instanceof DockNode) {
                DockNode node = (DockNode) item;
                DockTitleBar titleBar = node.getDockTitleBar();
                if (titleBar == null) {
                    log.info("{}{} = Item: {}, Parent: {} (Instance: {}, Label: {})", "\t".repeat(level), level, item.getClass().getSimpleName(), parent, item.hashCode(), "N/A");
                } else {
                    log.info("{}{} = Item: {}, Parent: {} (Instance: {}, Label: {})", "\t".repeat(level), level, item.getClass().getSimpleName(), parent, item.hashCode(), titleBar.getLabel().getText());
                }
            } else {
                log.info("{}{} = Item: {}, Parent: {} (Instance: {})", "\t".repeat(level), level, item.getClass().getSimpleName(), parent, item.hashCode());
            }

            if (item instanceof SplitPane) {
                recurse(((SplitPane) item).getItems(), level + 1);
            } else if (item instanceof DockNode) {
                recurse(((DockNode) item).getChildren(), level + 1);
            }
        }
    }


    private TreeView<String> generateRandomTree() {
        // create a demonstration tree view to use as the contents for a dock node
        TreeItem<String> root = new TreeItem<String>("Root");
        TreeView<String> treeView = new TreeView<String>(root);
        treeView.setShowRoot(false);

        // populate the prototype tree with some random nodes
        Random rand = new Random();
        for (int i = 4 + rand.nextInt(8); i > 0; i--) {
            TreeItem<String> treeItem = new TreeItem<String>("Item " + i);
            root.getChildren().add(treeItem);
            for (int j = 2 + rand.nextInt(4); j > 0; j--) {
                TreeItem<String> childItem = new TreeItem<String>("Child " + j);
                treeItem.getChildren().add(childItem);
            }
        }

        return treeView;
    }

    @Autowired
    public void setServerDetailsModel(ServerDetailsModel serverDetailsModel) {
        this.serverDetailsModel = serverDetailsModel;
    }
}
