package com.ibasco.sourcebuddy.components;

import com.ibasco.sourcebuddy.controllers.BaseController;
import com.ibasco.sourcebuddy.exceptions.NoMappedControllerException;
import com.ibasco.sourcebuddy.exceptions.ResourceLoadException;
import com.ibasco.sourcebuddy.exceptions.ViewLoadException;
import static com.ibasco.sourcebuddy.util.ResourceUtil.loadResource;
import com.ibasco.sourcebuddy.util.SpringUtil;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

@Component
public class ViewManager {

    private static final Logger log = LoggerFactory.getLogger(ViewManager.class);

    @Value("${app.enforce-fxml-controller-mapping}")
    private boolean enforceFxmlControllerMapping;

    private Map<String, ViewNodeControllerEntry> viewControllerMap = new HashMap<>();

    public boolean hasController(String viewName) {
        return viewControllerMap.containsKey(viewName);
    }

    @SuppressWarnings({"unchecked", "ClassEscapesDefinedScope"})
    public <T extends BaseController> T getController(String viewName) {
        ViewNodeControllerEntry entry = viewControllerMap.get(viewName);
        if (entry != null)
            return (T) entry.controller;
        return null;
    }

    @SuppressWarnings("unchecked")
    public <T extends Node> T getViewNode(String viewName) {
        ViewNodeControllerEntry entry = viewControllerMap.get(viewName);
        if (entry != null)
            return (T) entry.rootNode;
        return null;
    }

    @SuppressWarnings("ClassEscapesDefinedScope")
    public String getViewName(BaseController controller) {
        return viewControllerMap
                .entrySet().stream()
                .filter(e -> (e.getValue() != null) && (e.getValue().controller == controller))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    public <T extends Node> T loadView(String viewName) {
        T node;
        try {
            T bean = SpringUtil.getBean(viewName);
            log.debug("Loading view from cache: {}", viewName);
            return bean;
        } catch (Exception e) {
            node = loadFXML(viewName);
            SpringUtil.registerBean(viewName, node);
            log.debug("Loading view for the first time: {}", viewName);
        }
        return node;
    }

    private <T extends Node> T loadFXML(String viewName) {
        if (viewName == null || viewName.isBlank())
            throw new IllegalArgumentException("View name must not be null or blank");
        else if (viewName.endsWith(".fxml"))
            throw new IllegalArgumentException("View name should not end in .fxml");

        String viewPath;
        if (viewName.startsWith("/"))
            viewPath = String.format("/fxml%s.fxml", viewName);
        else
            viewPath = String.format("/fxml/%s.fxml", viewName);

        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setControllerFactory(SpringUtil.getContext()::getBean);
            URL viewUrl = loadResource(viewPath);
            loader.setLocation(viewUrl);
            T node = loader.load();
            //Add to the map
            BaseController controller = loader.getController();

            if (controller == null) {
                if (enforceFxmlControllerMapping)
                    throw new NoMappedControllerException(String.format("View '%s' does not have a controller mapping definition", viewName));
                log.warn("View '{}' does not have a valid controller mapping definition", viewName);
            } else {
                viewControllerMap.put(viewName, new ViewNodeControllerEntry(controller, node));
                log.debug("Added view/controller mapping (View Name: {}, Controller Class: {})", viewName, controller.getClass().getSimpleName());

                ChangeListener<Scene> sceneListener = new ChangeListener<>() {
                    @Override
                    public void changed(ObservableValue<? extends Scene> observable, Scene oldScene, Scene newScene) {
                        if (newScene != null) {
                            //log.debug("\tScene initialized (View: {}, Controller: {}, Stage: {})", viewName, controller.getClass().getSimpleName(), newScene.getWindow());
                            if (newScene.getWindow() != null) {
                                Stage stage = (Stage) newScene.getWindow();
                                //log.debug("\tStage initialized (View: {}, Controller: {}, Stage: {}, Scene: {})", viewName, controller.getClass().getSimpleName(), stage, newScene);
                                initializeAndRemove(controller, node, stage, node.sceneProperty(), this);
                                return;
                            }
                            newScene.windowProperty().addListener(new ChangeListener<>() {
                                @Override
                                public void changed(ObservableValue<? extends Window> observable, Window oldWindow, Window newWindow) {
                                    Stage stage = (Stage) newWindow;
                                    //log.debug("\tStage initialized (View: {}, Controller: {}, Stage: {}, Scene: {})", viewName, controller.getClass().getSimpleName(), stage, stage.getScene());
                                    initializeAndRemove(controller, node, stage, newScene.windowProperty(), this);
                                }
                            });
                        }
                    }
                };

                //Add event listener
                node.sceneProperty().addListener(sceneListener);
            }

            return node;
        } catch (IOException | ResourceLoadException e) {
            throw new ViewLoadException("Error loading view resource: " + viewName, e);
        }
    }

    @SuppressWarnings("unchecked")
    private void initializeAndRemove(BaseController controller, Node node, Stage stage, ObservableValue property, ChangeListener listener) {
        try {
            controller.initialize(stage, node);
            log.debug("ViewManager :: Initialized controller {}", controller.getClass().getSimpleName());
        } catch (Throwable ex) {
            throw new ViewLoadException(String.format("An exception occured during initialization of controller '%s'", controller.getClass().getName()), ex);
        } finally {
            property.removeListener(listener);
        }
    }

    private class ViewNodeControllerEntry {

        private BaseController controller;

        private Node rootNode;

        ViewNodeControllerEntry(BaseController controller, Node rootNode) {
            this.controller = controller;
            this.rootNode = rootNode;
        }
    }
}
