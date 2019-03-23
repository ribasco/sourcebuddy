package com.ibasco.sourcebuddy.controllers;

import com.ibasco.sourcebuddy.components.NotificationManager;
import com.ibasco.sourcebuddy.components.ServiceManager;
import com.ibasco.sourcebuddy.components.ViewManager;
import com.ibasco.sourcebuddy.constants.Beans;
import com.ibasco.sourcebuddy.events.ApplicationInitEvent;
import com.ibasco.sourcebuddy.util.SpringUtil;
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

        log.debug("Initialize :: Location: {}, Resource Bundle: {}", location, resourceBundle);
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

    public ViewManager getViewManager() {
        return viewManager;
    }

    @Autowired
    public void setViewManager(ViewManager viewManager) {
        this.viewManager = viewManager;
    }

    public ConfigurableApplicationContext getAppContext() {
        return applicationContext;
    }

    public NotificationManager getNotificationManager() {
        return notificationManager;
    }

    @Autowired
    public void setNotificationManager(NotificationManager notificationManager) {
        this.notificationManager = notificationManager;
    }

    public ServiceManager getServiceManager() {
        return serviceManager;
    }

    @Autowired
    public void setServiceManager(ServiceManager serviceManager) {
        this.serviceManager = serviceManager;
    }

    public HostServices getHostServices() {
        return applicationContext.getBean(Beans.HOST_SERVICES, HostServices.class);
    }

    protected void publishEvent(ApplicationEvent event) {
        SpringUtil.publishEvent(event);
    }

    @Autowired
    public void setApplicationContext(ConfigurableApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
}
