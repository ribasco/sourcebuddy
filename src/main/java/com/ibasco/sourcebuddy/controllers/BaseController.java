package com.ibasco.sourcebuddy.controllers;

import com.ibasco.sourcebuddy.components.NotificationManager;
import com.ibasco.sourcebuddy.components.ServiceManager;
import com.ibasco.sourcebuddy.components.ViewManager;
import com.ibasco.sourcebuddy.constants.Beans;
import javafx.application.HostServices;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Controller;

import java.net.URL;
import java.util.ResourceBundle;

@Controller
abstract public class BaseController implements Initializable {

    private static final Logger log = LoggerFactory.getLogger(BaseController.class);

    private ConfigurableApplicationContext applicationContext;

    private ViewManager viewManager;

    private NotificationManager notificationManager;

    private ServiceManager serviceManager;

    private URL location;

    private ResourceBundle resourceBundle;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.location = location;
        this.resourceBundle = resources;
    }

    /**
     * Called after the stage and scene objects have been initialized
     */
    abstract public void initialize(Stage stage, Node rootNode);

    protected URL getLocation() {
        return location;
    }

    protected ResourceBundle getResourceBundle() {
        return resourceBundle;
    }

    public ViewManager getViewManager() {
        return viewManager;
    }

    public ConfigurableApplicationContext getAppContext() {
        return applicationContext;
    }

    public NotificationManager getNotificationManager() {
        return notificationManager;
    }

    public ServiceManager getServiceManager() {
        return serviceManager;
    }

    public HostServices getHostServices() {
        return applicationContext.getBean(Beans.HOST_SERVICES, HostServices.class);
    }

    @Autowired
    public void setApplicationContext(ConfigurableApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Autowired
    public void setViewManager(ViewManager viewManager) {
        this.viewManager = viewManager;
    }

    @Autowired
    public void setNotificationManager(NotificationManager notificationManager) {
        this.notificationManager = notificationManager;
    }

    @Autowired
    public void setServiceManager(ServiceManager serviceManager) {
        this.serviceManager = serviceManager;
    }
}
