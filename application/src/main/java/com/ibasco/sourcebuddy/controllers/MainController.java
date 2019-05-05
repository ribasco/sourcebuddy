package com.ibasco.sourcebuddy.controllers;

import com.ibasco.sourcebuddy.components.DockManager;
import com.ibasco.sourcebuddy.components.TaskManager;
import com.ibasco.sourcebuddy.constants.Icons;
import com.ibasco.sourcebuddy.constants.Views;
import com.ibasco.sourcebuddy.domain.ConfigProfile;
import com.ibasco.sourcebuddy.domain.DockLayout;
import com.ibasco.sourcebuddy.events.ApplicationInitEvent;
import com.ibasco.sourcebuddy.gui.skins.CustomTaskProgressViewSkin;
import com.ibasco.sourcebuddy.model.AppModel;
import com.ibasco.sourcebuddy.util.ResourceUtil;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.controlsfx.control.*;
import org.dockfx.DockNode;
import org.dockfx.DockPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.URL;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
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

    private AppModel appModel;

    private PopOver serviceStatusPopOver;

    private TaskManager taskManager;

    private TaskProgressView<Task<?>> taskProgressView;

    private Button btnServiceStatus;

    private DockManager dockManager;

    @FXML
    private Menu menuView;

    @FXML
    private MenuItem miPreferences;

    @FXML
    private MenuItem miClearLayout;

    @FXML
    private Menu menuViewLayouts;

    @FXML
    private MenuItem miViewSaveLayout;

    @FXML
    private MenuItem miViewSaveLayoutAs;

    @FXML
    private MenuItem miViewResetLayout;

    private ToggleGroup menuLayoutToggleGroup;

    @FXML
    private MenuItem miViewSetDefault;

    @Override
    public void initialize(Stage stage, Node rootNode) {
        setupDocks(stage);
        setupMenu();
        setupTaskProgressView();
        updateSplitPaneResizable(dpMainDock.getChildren(), 0);
        setupMainToolbar();
        setupServiceStatusPopOver();
        setupStatusBar();
        publishEvent(new ApplicationInitEvent(this, stage));

        ConfigProfile profile = appModel.getActiveProfile();

        //Set active layout
        appModel.activeLayoutProperty().addListener(this::applyNewLayoutOnSelectionChange);
        log.info("Setting active layout from default: {} (Entries: {})", profile.getDefaultLayout(), profile.getDefaultLayout().getLayoutEntries().size());
        appModel.setActiveLayout(profile.getDefaultLayout());
    }

    private void onDockMenuSelection(ActionEvent actionEvent) {
        CheckMenuItem menuItem = (CheckMenuItem) actionEvent.getSource();
        DockNode dockNode = (DockNode) menuItem.getUserData();
        if (menuItem.isSelected()) {
            log.info("Docking node: {}", dockNode);
            dockManager.dockNode(dpMainDock, dockNode);
        } else {
            log.info("Un-docking node: {}", dockNode);
            dockManager.undockNode(dockNode);
        }
        log.info("Dock menu selection: {} (Source: {})", actionEvent, actionEvent.getSource());

    }

    private void setupMenu() {
        miPreferences.setOnAction(this::openPreferencesWindow);
        miClearLayout.setOnAction(event -> dockManager.clearDocks());
        menuLayoutToggleGroup = new ToggleGroup();
        menuLayoutToggleGroup.selectedToggleProperty().addListener(this::updateActiveLayout);

        miViewResetLayout.setOnAction(this::resetCurrentLayout);
        miViewSaveLayout.setOnAction(this::saveCurrentLayout);
        miViewSaveLayoutAs.setOnAction(this::saveCurrentLayoutAs);
        miViewSetDefault.setOnAction(this::setCurrentLayoutAsDefault);

        populateDockMenuItems();
        refreshLayoutMenuItems();
    }

    private void resetCurrentLayout(ActionEvent actionEvent) {
        if (appModel.getActiveLayout() == null) {
            log.warn("No active layout assigned");
            return;
        }
        Optional<DockLayout> res = dockManager.findLayoutById(appModel.getActiveLayout().getId());
        res.ifPresent(layout -> {
            log.info("Resetting active layout: {}", layout);
            appModel.setActiveLayout(layout);
        });
    }

    private void saveCurrentLayout(ActionEvent actionEvent) {
        DockLayout activeLayout = appModel.getActiveLayout();
        if (activeLayout == null)
            throw new IllegalStateException("No active layout set");
        log.info("Saving current layout: {}", activeLayout);
        activeLayout = dockManager.update(dpMainDock, activeLayout);
        appModel.setActiveLayout(activeLayout);
        log.info("Saved active layout with {} entries", activeLayout.getLayoutEntries().size());
    }

    private void saveCurrentLayoutAs(ActionEvent actionEvent) {
        TextInputDialog textInputDialog = new TextInputDialog();
        textInputDialog.setHeaderText("Enter layout name");
        textInputDialog.setContentText("Enter layout name");
        Optional<String> res = textInputDialog.showAndWait();
        if (res.isPresent()) {
            DockLayout newLayout = dockManager.create(dpMainDock, res.get());
            newLayout.setProfile(appModel.getActiveProfile());
            appModel.getActiveProfile().getDockLayouts().add(newLayout);
            log.info("Saved new layout: {}", newLayout);
            saveActiveProfile();
            refreshLayoutMenuItems();
            appModel.setActiveLayout(newLayout);
        }
    }

    private void saveActiveProfile() {
        appModel.setActiveProfile(configService.saveProfile(appModel.getActiveProfile()));
    }

    private void updateActiveLayout(ObservableValue<? extends Toggle> observableValue, Toggle oldValue, Toggle newValue) {
        if (newValue != null) {
            int layoutId = (int) newValue.getUserData();
            log.info("Selected layout: {}", layoutId);
            Optional<DockLayout> res = dockManager.findLayoutById(layoutId);
            res.ifPresent(layout -> appModel.setActiveLayout(layout));
        }
    }

    private void applyNewLayoutOnSelectionChange(ObservableValue<? extends DockLayout> observableValue, DockLayout oldValue, DockLayout newValue) {
        if (newValue != null) {
            log.info("Applying layout: {}", newValue);
            dockManager.applyLayout(dpMainDock, newValue);
            log.info("Updating active layout menu selection");
            //Update selection
            for (MenuItem menuItem : menuViewLayouts.getItems()) {
                RadioMenuItem radioMenuItem = (RadioMenuItem) menuItem;
                int layoutId = (int) menuItem.getUserData();
                radioMenuItem.setSelected(newValue.getId() == layoutId);
            }
        }
    }

    private void setCurrentLayoutAsDefault(ActionEvent actionEvent) {
        ConfigProfile activeProfile = appModel.getActiveProfile();
        activeProfile.setDefaultLayout(appModel.getActiveLayout());
        saveActiveProfile();
        log.info("Set default layout to '{}'", activeProfile.getDefaultLayout().getName());
    }

    private void refreshLayoutMenuItems() {
        log.info("Refreshing layout menu items");
        ConfigProfile profile = appModel.getActiveProfile();

        menuViewLayouts.getItems().clear();
        for (DockLayout layout : profile.getDockLayouts()) {
            RadioMenuItem miLayout = new RadioMenuItem(layout.getName());
            miLayout.setToggleGroup(menuLayoutToggleGroup);
            miLayout.setUserData(layout.getId());
            if (appModel.getActiveLayout() != null && appModel.getActiveLayout().equals(layout)) {
                miLayout.setSelected(true);
            }
            menuViewLayouts.getItems().add(miLayout);
        }
    }

    private void populateDockMenuItems() {
        Map<String, DockNode> beanMap = getAppContext().getBeansOfType(DockNode.class);
        if (beanMap != null) {
            menuView.getItems().add(new SeparatorMenuItem());
            //Populate view menu
            for (Map.Entry<String, DockNode> entry : beanMap.entrySet()) {
                DockNode dockNode = entry.getValue();
                CheckMenuItem miDock = new CheckMenuItem(dockNode.getTitle());
                miDock.selectedProperty().bindBidirectional(dockNode.dockedProperty());
                miDock.setOnAction(this::onDockMenuSelection);
                miDock.setUserData(entry.getValue());
                menuView.getItems().add(miDock);
            }
        }
    }

    private void openPreferencesWindow(ActionEvent actionEvent) {
        Parent preferencesView = getViewManager().loadView(Views.PREFERENCES);
        Scene scene;
        if (preferencesView.getScene() == null) {
            scene = new Scene(preferencesView);
        } else {
            scene = preferencesView.getScene();
        }
        URL res = ResourceUtil.loadResource("/styles/default.css");
        scene.getStylesheets().setAll(res.toExternalForm());
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();
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
        //dpMainDock.addEventHandler(DockEvent.DOCK_ENTER, event -> log.info("Dock entered"));
        //dpMainDock.addEventHandler(DockEvent.DOCK_RELEASED, event -> log.info("Dock released"));
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
    public void setAppModel(AppModel appModel) {
        this.appModel = appModel;
    }

    @Autowired
    public void setTaskManager(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @Autowired
    public void setDockManager(DockManager dockManager) {
        this.dockManager = dockManager;
    }
}
