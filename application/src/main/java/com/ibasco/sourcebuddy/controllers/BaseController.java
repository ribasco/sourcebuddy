package com.ibasco.sourcebuddy.controllers;

import com.ibasco.sourcebuddy.annotations.AbstractController;
import com.ibasco.sourcebuddy.components.*;
import static com.ibasco.sourcebuddy.components.GuiHelper.findNotificationPane;
import com.ibasco.sourcebuddy.constants.Beans;
import com.ibasco.sourcebuddy.events.ApplicationInitEvent;
import com.ibasco.sourcebuddy.service.ConfigService;
import javafx.application.HostServices;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.stage.Stage;
import org.controlsfx.control.NotificationPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.EventListener;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

@AbstractController
abstract public class BaseController implements Initializable {

    private static final Logger log = LoggerFactory.getLogger(BaseController.class);

    private URL location;

    private ResourceBundle resourceBundle;

    private Stage stage;

    private Parent rootNode;

    private GuiHelper guiHelper;

    SpringHelper springHelper;

    ConfigurableApplicationContext applicationContext;

    ConfigService configService;

    ViewManager viewManager;

    NotificationManager notificationManager;

    TaskManager taskManager;

    static Map<Stage, NotificationPane> paneCache = new HashMap<>();

    @Override
    public final void initialize(URL location, ResourceBundle resources) {
        this.location = location;
        this.resourceBundle = resources;
        log.debug("=============================================================");
        log.debug("VIEW INITIALIZE: {}", this.getClass().getSimpleName());
        log.debug("=============================================================");
    }

    /**
     * This method should not be used by subclasses. This is exclusive for the ViewManager
     *
     * @param stage
     *         The stage associated with this controller
     * @param node
     *         The root node associated with the stage/scene
     */
    public final void preInit(Stage stage, Parent node) {
        this.stage = stage;
        this.rootNode = node;
        NotificationPane pane = paneCache.computeIfAbsent(stage, stage1 -> findNotificationPane(node));

        if (!(this instanceof PreloadController)) {
            if (pane != null) {
                log.debug("Found pane for {} for stage {}", getClass().getSimpleName(), stage);
                this.notificationManager = applicationContext.getBean(NotificationManager.class, pane);
            } else {
                String msg = String.format("The controller '%s' is not associated with a notification pane", getClass().getSimpleName());
                log.error(msg);
                throw new IllegalStateException(msg);
            }
        }

        //this.notificationManager = applicationContext.getBean(NotificationManager.class, );
        log.debug("=============================================================");
        log.debug("STAGE/SCENE INITIALIZE: {}", this.getClass().getSimpleName());
        log.debug("=============================================================");
        initialize(stage, node);
    }

    /**
     * Called after the stage and scene objects have been initialized
     */
    abstract public void initialize(Stage stage, Node rootNode);

    @EventListener
    private void onMainInitialized(ApplicationInitEvent initEvent) {
        onAppInitialized();
    }

    /**
     * Called after the main controller has initializeed
     */
    protected void onAppInitialized() {
        //no-op
    }

    protected Stage getStage() {
        return stage;
    }

    protected Parent getRootNode() {
        return rootNode;
    }

    protected URL getLocation() {
        return location;
    }

    protected ResourceBundle getResourceBundle() {
        return resourceBundle;
    }

    ViewManager getViewManager() {
        return viewManager;
    }

    public ConfigurableApplicationContext getAppContext() {
        return applicationContext;
    }

    NotificationManager getNotificationManager() {
        return notificationManager;
    }

    public HostServices getHostServices() {
        return applicationContext.getBean(Beans.HOST_SERVICES, HostServices.class);
    }

    void publishEvent(ApplicationEvent event) {
        springHelper.publishEvent(event);
    }

    protected GuiHelper getGuiHelper() {
        return guiHelper;
    }

    protected ConfigService getConfigService() {
        return configService;
    }

    @Autowired
    protected void setGuiHelper(GuiHelper guiHelper) {
        this.guiHelper = guiHelper;
    }

    @Autowired
    protected void setApplicationContext(ConfigurableApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Autowired
    protected void setViewManager(ViewManager viewManager) {
        this.viewManager = viewManager;
    }

    @Autowired
    protected void setTaskManager(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @Autowired
    protected void setSpringHelper(SpringHelper springHelper) {
        this.springHelper = springHelper;
    }

    @Autowired
    protected void setConfigService(ConfigService configService) {
        this.configService = configService;
    }
}
