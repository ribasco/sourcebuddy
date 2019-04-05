package com.ibasco.sourcebuddy.controllers;

import com.ibasco.sourcebuddy.components.NotificationManager;
import com.ibasco.sourcebuddy.components.SpringHelper;
import com.ibasco.sourcebuddy.components.TaskManager;
import com.ibasco.sourcebuddy.components.ViewManager;
import com.ibasco.sourcebuddy.constants.Beans;
import com.ibasco.sourcebuddy.events.ApplicationInitEvent;
import javafx.application.HostServices;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.EventListener;

import java.net.URL;
import java.util.ResourceBundle;

abstract public class BaseController implements Initializable {

    private static final Logger log = LoggerFactory.getLogger(BaseController.class);

    private URL location;

    private ResourceBundle resourceBundle;

    SpringHelper springHelper;

    ConfigurableApplicationContext applicationContext;

    ViewManager viewManager;

    NotificationManager notificationManager;

    TaskManager taskManager;

    @Override
    public final void initialize(URL location, ResourceBundle resources) {
        this.location = location;
        this.resourceBundle = resources;
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

    @Autowired
    protected void setApplicationContext(ConfigurableApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Autowired
    protected void setViewManager(ViewManager viewManager) {
        this.viewManager = viewManager;
    }

    @Autowired
    protected void setNotificationManager(NotificationManager notificationManager) {
        this.notificationManager = notificationManager;
    }

    @Autowired
    protected void setTaskManager(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @Autowired
    protected void setSpringHelper(SpringHelper springHelper) {
        this.springHelper = springHelper;
    }
}
