package com.ibasco.sourcebuddy.config;

import com.ibasco.sourcebuddy.components.ViewManager;
import com.ibasco.sourcebuddy.constants.Icons;
import com.ibasco.sourcebuddy.constants.Views;
import com.ibasco.sourcebuddy.util.ResourceUtil;
import javafx.scene.control.SplitPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import org.dockfx.DockNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DockConfig {

    private ViewManager viewManager;

    public static final String DOCK_SERVER_BROWSER = "serverBrowserDock";

    public static final String DOCK_PLAYER_BROWSER = "playerBrowserDock";

    public static final String DOCK_RULES_BROWSER = "rulesBrowserDock";

    public static final String DOCK_RCON = "rconDock";

    public static final String DOCK_LOGS = "logsDock";

    public static final String DOCK_CHAT = "chatDock";

    public static final String DOCK_SERVER_INFO = "serverInfoDock";

    public static final String DOCK_GAME_BROWSER = "gameBrowserDock";

    public static final String DOCK_SOURCEMOD = "sourceModDock";

    @Bean
    public DockNode sourceModDock() {
        Pane sourceModPane = viewManager.loadView(Views.DOCK_SOURCEMOD);
        DockNode sourceModDock = new DockNode(sourceModPane, "SourceMod", null);
        return sourceModDock;
    }

    @Bean
    public DockNode serverBrowserDock() {
        ImageView serverBrowserImage = ResourceUtil.loadIconView(Icons.SERVER_BROWSER_ICON);
        Pane serverBrowserPane = viewManager.loadView(Views.DOCK_SERVER_BROWSER);
        DockNode serverBrowserDock = new DockNode(serverBrowserPane, "Servers", serverBrowserImage);
        SplitPane.setResizableWithParent(serverBrowserDock, true);
        serverBrowserDock.setDockTitleBar(null);
        serverBrowserDock.setPrefWidth(1300);
        return serverBrowserDock;
    }

    @Bean
    public DockNode playerBrowserDock() {
        ImageView playerBrowserImage = ResourceUtil.loadIconView(Icons.PLAYER_BROWSER_ICON);
        Pane playerBrowserPane = viewManager.loadView(Views.DOCK_PLAYER_BROWSER);
        DockNode playerBrowserDock = new DockNode(playerBrowserPane, "Players", playerBrowserImage);
        SplitPane.setResizableWithParent(playerBrowserDock, false);
        playerBrowserDock.setMinWidth(350);
        playerBrowserDock.setPrefWidth(350);
        return playerBrowserDock;
    }

    @Bean
    public DockNode rulesBrowserDock() {
        ImageView rulesBrowserImage = ResourceUtil.loadIconView(Icons.RULES_BROWSER_ICON);
        Pane rulesBrowserPane = viewManager.loadView(Views.DOCK_RULES_BROWSER);
        DockNode rulesBrowserDock = new DockNode(rulesBrowserPane, "Rules", rulesBrowserImage);
        SplitPane.setResizableWithParent(rulesBrowserDock, false);
        rulesBrowserDock.setMinWidth(350);
        rulesBrowserDock.setPrefWidth(350);
        return rulesBrowserDock;
    }

    @Bean
    public DockNode rconDock() {
        ImageView rconImage = ResourceUtil.loadIconView(Icons.SERVER_MANAGER_ICON);
        Pane rconPane = viewManager.loadView(Views.DOCK_RCON);
        return new DockNode(rconPane, "Rcon", rconImage);
    }

    @Bean
    public DockNode logsDock() {
        ImageView logsImage = ResourceUtil.loadIconView(Icons.LOGS_ICON);
        Pane logsPane = viewManager.loadView(Views.DOCK_LOGS);
        return new DockNode(logsPane, "Logs", logsImage);
    }

    @Bean
    public DockNode chatDock() {
        ImageView chatImage = ResourceUtil.loadIconView(Icons.CHAT_ICON);
        Pane serverChatPane = viewManager.loadView(Views.DOCK_SERVER_CHAT);
        return new DockNode(serverChatPane, "Chat", chatImage);
    }

    @Bean
    public DockNode serverInfoDock() {
        Pane serverInfoPane = viewManager.loadView(Views.DOCK_SERVER_INFO);
        return new DockNode(serverInfoPane, "Server Info");
    }

    @Bean
    public DockNode gameBrowserDock() {
        Pane gameBrowserPane = viewManager.loadView(Views.DOCK_GAME_BROWSER);
        DockNode gameBrowserDock = new DockNode(gameBrowserPane, "Game Browser");
        gameBrowserDock.setPrefWidth(230);
        gameBrowserDock.setMinWidth(250);
        return gameBrowserDock;
    }

    @Autowired
    public void setViewManager(ViewManager viewManager) {
        this.viewManager = viewManager;
    }
}
