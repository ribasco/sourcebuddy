package com.ibasco.sourcebuddy.components;

import static com.ibasco.sourcebuddy.components.GuiHelper.findNode;
import com.ibasco.sourcebuddy.constants.Views;
import com.ibasco.sourcebuddy.controllers.BaseController;
import com.ibasco.sourcebuddy.controllers.FragmentController;
import com.ibasco.sourcebuddy.exceptions.NoMappedControllerException;
import com.ibasco.sourcebuddy.exceptions.ResourceLoadException;
import com.ibasco.sourcebuddy.exceptions.ViewLoadException;
import com.ibasco.sourcebuddy.gui.tableview.cells.ViewFragmentCell;
import static com.ibasco.sourcebuddy.util.ResourceUtil.loadResource;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

@Component
public class ViewManager {

    private static final Logger log = LoggerFactory.getLogger(ViewManager.class);

    @Value("${app.enforce-fxml-controller-mapping}")
    private boolean enforceFxmlControllerMapping;

    private Map<String, ViewNodeControllerEntry> viewControllerMap = new HashMap<>();

    private ApplicationContext applicationContext;

    private SpringHelper springHelper;

    public boolean hasController(String viewName) {
        return viewControllerMap.containsKey(viewName);
    }

    @SuppressWarnings({"unchecked"})
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

    public String getViewName(BaseController controller) {
        return viewControllerMap
                .entrySet().stream()
                .filter(e -> (e.getValue() != null) && (e.getValue().controller == controller))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    public <A, B, C extends FragmentController, D extends ViewFragmentCell<A, B, C>> D loadViewFragmentCell(Class<D> cls, String viewFragmentName, Class<C> controllerClass) {
        try {
            Constructor<D> ctr = ReflectionUtils.accessibleConstructor(cls);
            Method ctrSetterMethod = cls.getSuperclass().getDeclaredMethod("setController", FragmentController.class);
            ctrSetterMethod.setAccessible(true);
            D cellInstance = ctr.newInstance();
            C controller = loadViewFragment(String.format("/fragments/%s", viewFragmentName), controllerClass);
            ctrSetterMethod.invoke(cellInstance, controller);
            return cellInstance;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        return applicationContext.getBean(cls, viewFragmentName, controllerClass);
    }

    public Parent getPlaceholderView(String message) {
        return getPlaceholderView(Bindings.format(message), null);
    }

    public Parent getPlaceholderView(ObservableValue<String> progressMsg, EventHandler<ActionEvent> cancelAction) {
        return getPlaceholderView(null, progressMsg, cancelAction);
    }

    public Parent getPlaceholderView(ObservableValue<String> titleMsg, ObservableValue<String> progressMsg, EventHandler<ActionEvent> cancelAction) {
        VBox placeholderView = loadDetachedView(Views.FRAGMENT_PLACEHOLDER);
        Label title = findNode(placeholderView, Label.class, "lblTitle");
        if (titleMsg != null) {
            title.setVisible(true);
            title.textProperty().bind(titleMsg);
        } else {
            title.setVisible(false);
            title.textProperty().unbind();
        }
        Label progress = findNode(placeholderView, Label.class, "lblProgress");
        if (progressMsg != null) {
            progress.setVisible(true);
            progress.textProperty().bind(progressMsg);
        } else {
            progress.setVisible(false);
            progress.textProperty().unbind();
        }
        Button cancelButton = findNode(placeholderView, Button.class, "btnCancel");
        if (cancelAction != null) {
            cancelButton.setManaged(true);
            cancelButton.setVisible(true);
            cancelButton.setOnAction(cancelAction);
        } else {
            cancelButton.setManaged(false);
            cancelButton.setVisible(false);
        }
        return placeholderView;
    }

    public <T extends Node> T loadDetachedView(String viewName) {
        FXMLLoader loader = new FXMLLoader();
        T node;
        try {
            loader.setLocation(loadResource(resolveViewPath(viewName)));
            node = loader.load();
        } catch (IOException e) {
            throw new ViewLoadException("Error loading detached view", e);
        }
        return node;
    }

    public <T extends FragmentController> T loadViewFragment(String viewName, Class<T> controller) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(loadResource(resolveViewPath(viewName)));
            T fragmentController = null;
            if (controller != null) {
                fragmentController = applicationContext.getBean(controller);
                loader.setController(fragmentController);
            }
            Parent node = loader.load();
            if (fragmentController != null)
                fragmentController.setRootNode(node);
            return fragmentController;
        } catch (IOException e) {
            throw new ViewLoadException("Error loading detached view", e);
        }
    }

    public <T extends Node> T loadView(String viewName) {
        String beanName = viewName + "View";
        T node;
        try {
            T bean = springHelper.getBean(beanName);
            log.debug("Loading view from cache: {}", beanName);
            return bean;
        } catch (Exception e) {
            node = loadFXML(viewName);
            springHelper.registerBean(beanName, node);
            log.debug("Loading view for the first time: {}", beanName);
        }
        return node;
    }

    private <T extends Node> T loadFXML(String viewName) {
        if (viewName == null || viewName.isBlank())
            throw new IllegalArgumentException("View name must not be null or blank");
        else if (viewName.endsWith(".fxml"))
            throw new IllegalArgumentException("View name should not end in .fxml");

        String viewPath = resolveViewPath(viewName);

        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setControllerFactory(applicationContext::getBean);
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
                            if (newScene.getWindow() != null) {
                                Stage stage = (Stage) newScene.getWindow();
                                initializeAndRemove(controller, node, stage, node.sceneProperty(), this);
                                return;
                            }
                            newScene.windowProperty().addListener(new ChangeListener<>() {
                                @Override
                                public void changed(ObservableValue<? extends Window> observable, Window oldWindow, Window newWindow) {
                                    Stage stage = (Stage) newWindow;
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

    private String resolveViewPath(String viewName) {
        String viewPath;
        if (viewName.startsWith("/"))
            viewPath = String.format("/fxml%s.fxml", viewName);
        else
            viewPath = String.format("/fxml/%s.fxml", viewName);
        return viewPath;
    }

    @SuppressWarnings("unchecked")
    private void initializeAndRemove(BaseController controller, Node node, Stage stage, ObservableValue property, ChangeListener listener) {
        try {
            controller.preInit(stage, (Parent) node);
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

    @Autowired
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Autowired
    public void setSpringHelper(SpringHelper springHelper) {
        this.springHelper = springHelper;
    }
}
