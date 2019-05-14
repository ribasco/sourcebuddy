package com.ibasco.sourcebuddy.controllers;

import com.ibasco.sourcebuddy.components.GuiHelper;
import static com.ibasco.sourcebuddy.components.GuiHelper.*;
import com.ibasco.sourcebuddy.constants.Views;
import com.ibasco.sourcebuddy.controls.SortedCheckComboBox;
import com.ibasco.sourcebuddy.domain.*;
import com.ibasco.sourcebuddy.enums.MiscFilters;
import com.ibasco.sourcebuddy.enums.OperatingSystem;
import com.ibasco.sourcebuddy.enums.ServerStatus;
import com.ibasco.sourcebuddy.gui.converters.MappedObjectStringConverter;
import com.ibasco.sourcebuddy.gui.listeners.ToggableSplitPaneChangeListener;
import com.ibasco.sourcebuddy.gui.tableview.factory.ServerBrowserTableViewFactory;
import com.ibasco.sourcebuddy.gui.tableview.rows.HighlightRow;
import com.ibasco.sourcebuddy.gui.treetableview.cells.FormattedTreeTableCell;
import com.ibasco.sourcebuddy.gui.treetableview.factory.BookmarksTreeTableCellFactory;
import com.ibasco.sourcebuddy.model.AppModel;
import com.ibasco.sourcebuddy.model.ServerFilterModel;
import com.ibasco.sourcebuddy.model.SteamAppsModel;
import com.ibasco.sourcebuddy.service.ServerManager;
import com.ibasco.sourcebuddy.service.impl.SingleServerDetailsRefreshService;
import com.ibasco.sourcebuddy.tasks.*;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialogLayout;
import com.jfoenix.controls.JFXTabPane;
import com.jfoenix.controls.JFXToggleButton;
import com.jfoenix.controls.events.JFXDialogEvent;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.CheckComboBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Function;
import java.util.function.Predicate;

@SuppressWarnings("Duplicates")
@Controller
public class ServerBrowserController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(ServerBrowserController.class);

    //<editor-fold desc="FXML Properties">
    @FXML
    private TableView<ServerDetails> tvServerBrowser;

    @FXML
    private TreeTableView<ServerDetails> ttvBookmarkedServers;

    @FXML
    private TreeTableView<ServerDetails> ttvManagedServers;

    @FXML
    private Button btnSetDefaultGame;

    @FXML
    private Button btnRefreshServerList;

    @FXML
    private JFXTabPane tpServers;

    @FXML
    private ToolBar tbServerPanel;

    @FXML
    private Tab tabBookmarks;

    @FXML
    private Tab tabManagedServers;

    @FXML
    private Tab tabServerBrowser;

    @FXML
    private StackPane spServerBrowser;

    @FXML
    private Button btnAddServer;

    @FXML
    private TextField tfFilterText;

    @FXML
    private SortedCheckComboBox<String> cbFilterTags;

    @FXML
    private SortedCheckComboBox<Country> cbFilterCountry;

    @FXML
    private SortedCheckComboBox<String> cbFilterMaps;

    @FXML
    private SortedCheckComboBox<ServerStatus> cbFilterStatus;

    @FXML
    private SortedCheckComboBox<MiscFilters> cbFilterMisc;

    @FXML
    private SortedCheckComboBox<OperatingSystem> cbFilterOs;

    @FXML
    private Button btnUpdateServerList;

    @FXML
    private Label lblSelectedGame;

    @FXML
    private TextField tfSearchPlayer;

    @FXML
    private Button btnClearFilters;
    //</editor-fold>

    private AppModel appModel;

    private SteamAppsModel steamGamesModel;

    private BookmarksTreeTableCellFactory bookmarksTreeTableViewFactory;

    private ServerBrowserTableViewFactory serverBrowserTableCellFactory;

    private SingleServerDetailsRefreshService singleUpdateService;

    private ServerManager serverManager;

    private ObjectProperty<Predicate<ServerDetails>> serverDetailsPredicate = new SimpleObjectProperty<>(p -> true);

    private ServerFilterModel serverFilterModel;

    private ListProperty<ServerDetails> filteredServerList = new SimpleListProperty<>();

    @FXML
    private TextField tfMsIpAddress;

    @FXML
    private TextField tfMsPort;

    @FXML
    private TextField tfMsCurrentMap;

    @FXML
    private TextField tfMsStatus;

    @FXML
    private TextField tfMsPlayerCount;

    @FXML
    private TextField tfMsMaxPlayerCount;

    @FXML
    private TextField tfMsLatency;

    @FXML
    private TextField tfMsVersion;

    @FXML
    private Label lblMsServerName;

    @FXML
    private Label lblMsHeader;

    @FXML
    private PasswordField tfMsRconPassword;

    @FXML
    private Button btnMsTestRcon;

    @FXML
    private JFXToggleButton tbShowDetailPane;

    @FXML
    private Button btnRefreshManagedServers;

    @FXML
    private AnchorPane apServerSettings;

    @FXML
    private Button btnSaveManagedServer;

    @FXML
    private Spinner<Integer> spMsLogListenPort;

    @FXML
    private TextField tfMsLogListenIP;

    @FXML
    private SplitPane spManagedServers;

    @Override
    public void initialize(Stage stage, Node rootNode) {
        setupServerTabPanel();
        setupFilterComponents();
        setupServerBrowserTable();
        setupBookmarksTable();
        setupManagedServersTable();
        setupManagedServerDetailBindings();
        setupSingleServerUpdateService();
        setupButtons();

        lblSelectedGame.textProperty().bind(Bindings.format("%s (Total: %d)", steamGamesModel.selectedGameProperty(), filteredServerList.sizeProperty()));
        tpServers.setTabDragPolicy(TabPane.TabDragPolicy.REORDER);
        steamGamesModel.selectedGameProperty().addListener(this::updateSteamAppServerEntries);
        steamGamesModel.setSelectedGame(appModel.getActiveProfile().getDefaultGame());

        refreshServerListContent();
        bindToggablePane(spManagedServers, apServerSettings, tbShowDetailPane, appModel.getActiveProfile().showSettingsPaneProperty());
    }

    private void bindToggablePane(SplitPane splitPane, Node toggableNode, ToggleButton toggleButton, BooleanProperty property) {
        ToggableSplitPaneChangeListener listener = new ToggableSplitPaneChangeListener(splitPane, toggableNode);
        property.addListener(listener);
        toggleButton.selectedProperty().bindBidirectional(property);
        listener.changed(property, null, property.get());
    }

    @Override
    protected boolean onStageClosing(Stage stage) {
        log.info("onStageClosing() :: Detected stage close request. Cancelling");

        log.info("Saving active profile");
        ConfigProfile activeProfile = appModel.getActiveProfile();
        configService.saveProfile(activeProfile);

        if (taskManager.getRunningTasks() > 0) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "There are still tasks running in the background, Are you sure you want to continue?", ButtonType.YES, ButtonType.CANCEL);
            Optional<ButtonType> res = alert.showAndWait();
            if (res.isPresent() && ButtonType.YES.equals(res.get())) {
                for (Task task : taskManager.getAllTasks()) {
                    log.info("Cancelling task '{}'", task.getClass().getSimpleName());
                    task.cancel(true);
                }
                return true;
            }
        }
        return true;
    }

    private void setupManagedServerDetailBindings() {
        bindManagedServerObservable(lblMsHeader.textProperty(), ServerDetails::getName);
        bindManagedServerObservable(lblMsServerName.textProperty(), ServerDetails::getName);
        bindManagedServerObservable(tfMsStatus.textProperty(), ServerDetails::getStatus, Enum::name);
        bindManagedServerObservable(tfMsIpAddress.textProperty(), ServerDetails::getIpAddress);
        bindManagedServerObservable(tfMsPort.textProperty(), ServerDetails::getPort, String::valueOf);
        bindManagedServerObservable(tfMsCurrentMap.textProperty(), ServerDetails::getMapName);
        bindManagedServerObservable(tfMsPlayerCount.textProperty(), ServerDetails::getPlayerCount, String::valueOf);
        bindManagedServerObservable(tfMsMaxPlayerCount.textProperty(), ServerDetails::getMaxPlayerCount, String::valueOf);
        bindManagedServerObservable(tfMsLatency.textProperty(), ServerDetails::getLatency, String::valueOf);

        //bindManagedServerDetails(tfMsRconPassword.textProperty(), ManagedServer::getRconPassword, null);

        btnSaveManagedServer.setOnAction(this::saveManagedServerDetails);
        spMsLogListenPort.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1000, 65535, 43813));
        appModel.selectedServerProperty().addListener(this::updatePropertyBindingsOnSelection);
    }

    private void updatePropertyBindingsOnSelection(ObservableValue<? extends ServerDetails> observable, ServerDetails oldValue, ServerDetails newValue) {
        if (oldValue != null) {
            tfMsRconPassword.textProperty().unbind();
            tfMsLogListenIP.textProperty().unbind();
            spMsLogListenPort.getValueFactory().valueProperty().unbind();
        }
        if (newValue != null && serverManager.isManaged(newValue)) {
            ManagedServer managedServer = serverManager.findManagedServer(newValue);
            if (managedServer != null) {
                tfMsRconPassword.textProperty().bindBidirectional(managedServer.rconPasswordProperty());
                tfMsLogListenIP.textProperty().bindBidirectional(managedServer.logListenIpProperty());
                spMsLogListenPort.getValueFactory().valueProperty().bindBidirectional(managedServer.logListenPortProperty().asObject());
                appModel.setSelectedManagedServer(managedServer);
            }
        }
    }

    private void saveManagedServerDetails(ActionEvent actionEvent) {
        if (appModel.getSelectedManagedServer() == null)
            return;
        log.info("Saving managed server details");
        ManagedServer managedServer = appModel.getSelectedManagedServer();
        appModel.setSelectedManagedServer(serverManager.save(managedServer));
    }

    private <T> void bindManagedServerObservable(Property<String> target, Function<ServerDetails, T> mapper) {
        bindManagedServerObservable(target, mapper, null);
    }

    private <T> void bindManagedServerObservable(Property<String> target, Function<ServerDetails, T> mapper, Function<T, String> converter) {
        target.bind(Bindings.createStringBinding(() -> {
            ServerDetails selectedServer = appModel.getSelectedServer();
            if (tabManagedServers.isSelected() && serverManager.isManaged(selectedServer)) {
                if (converter == null) {
                    return String.valueOf(mapper.apply(selectedServer));
                }
                return converter.apply(mapper.apply(selectedServer));
            }
            return "N/A";
        }, appModel.selectedServerProperty()));
    }

    private <T> void bindManagedServerDetails(Property<String> target, Function<ManagedServer, T> mapper, Function<T, String> converter) {
        target.bind(Bindings.createStringBinding(() -> {
            ServerDetails selectedServer = appModel.getSelectedServer();
            ManagedServer managedServer = serverManager.findManagedServer(selectedServer);
            if (tabManagedServers.isSelected() && serverManager.isManaged(selectedServer)) {
                if (converter == null) {
                    return String.valueOf(mapper.apply(managedServer));
                }
                return converter.apply(mapper.apply(managedServer));
            }
            return "N/A";
        }, appModel.selectedServerProperty()));
    }

    private void clearFilters(ActionEvent actionEvent) {
        cbFilterOs.getCheckModel().clearChecks();
        cbFilterStatus.getCheckModel().clearChecks();
        cbFilterTags.getCheckModel().clearChecks();
        cbFilterCountry.getCheckModel().clearChecks();
        cbFilterMaps.getCheckModel().clearChecks();
        cbFilterMisc.getCheckModel().clearChecks();
        tfFilterText.clear();
        tfSearchPlayer.clear();
    }

    private void setupFilterComponents() {
        serverDetailsPredicate.bind(createServerDetailsPredicateBinding());

        clearOnNewListSelection(serverFilterModel.getServerTags(), serverFilterModel.getCountries(), serverFilterModel.getMaps());

        appModel.serverDetailsProperty().addListener((ListChangeListener<ServerDetails>) c -> {
            while (c.next()) {
                if (c.wasUpdated()) {
                    for (int i = c.getFrom(); i < c.getTo(); ++i) {
                        ServerDetails details = appModel.getServerDetails().get(i);
                        if (details == null)
                            continue;
                        log.debug("Updated: {}", details);
                        List<String> tags = GuiHelper.extractServerTags(details);
                        if (!tags.isEmpty())
                            serverFilterModel.getServerTags().addAll(tags);
                        if (details.getCountry() != null)
                            serverFilterModel.getCountries().add(details.getCountry());
                        if (!StringUtils.isBlank(details.getMapName()))
                            serverFilterModel.getMaps().add(details.getMapName());
                    }
                } else {
                    for (ServerDetails details : c.getAddedSubList()) {
                        if (details == null)
                            continue;
                        List<String> tags = GuiHelper.extractServerTags(details);
                        if (!tags.isEmpty())
                            serverFilterModel.getServerTags().addAll(tags);
                        if (details.getCountry() != null)
                            serverFilterModel.getCountries().add(details.getCountry());
                        if (!StringUtils.isBlank(details.getMapName()))
                            serverFilterModel.getMaps().add(details.getMapName());
                    }
                }
            }
        });

        initializeFilterCheckComboBox(cbFilterStatus, ServerStatus.values(), ServerStatus::getDescription);
        initializeFilterCheckComboBox(cbFilterOs, OperatingSystem.values(), OperatingSystem::getName);
        initializeFilterCheckComboBox(cbFilterMisc, MiscFilters.values(), MiscFilters::getDescription);

        //Bind selected properties to the model
        bindCheckComboBoxSelection(cbFilterTags, serverFilterModel.getSelectedServerTags());
        bindCheckComboBoxSelection(cbFilterCountry, serverFilterModel.getSelectedCountries());
        bindCheckComboBoxSelection(cbFilterMaps, serverFilterModel.getSelectedMaps());
        bindCheckComboBoxSelection(cbFilterStatus, serverFilterModel.getSelectedStatus());
        bindCheckComboBoxSelection(cbFilterOs, serverFilterModel.getSelectedOs());
        bindCheckComboBoxSelection(cbFilterMisc, serverFilterModel.getSelectedMiscFilters());

        //Bind and populate content
        GuiHelper.bindContent(cbFilterTags.getBackingList(), serverFilterModel.getServerTags());
        GuiHelper.bindContent(cbFilterCountry.getBackingList(), serverFilterModel.getCountries());
        GuiHelper.bindContent(cbFilterMaps.getBackingList(), serverFilterModel.getMaps());
        GuiHelper.bindContent(cbFilterStatus.getBackingList(), FXCollections.observableSet(ServerStatus.values()));
        GuiHelper.bindContent(cbFilterOs.getBackingList(), FXCollections.observableSet(OperatingSystem.values()));
        GuiHelper.bindContent(cbFilterMisc.getBackingList(), FXCollections.observableSet(MiscFilters.values()));
    }

    private <T> void initializeFilterCheckComboBox(CheckComboBox<T> comboBox, T[] values, Function<T, String> labelMapper) {
        comboBox.setConverter(new MappedObjectStringConverter<>(labelMapper));
    }

    private void clearOnNewListSelection(Collection<?>... collections) {
        appModel.serverDetailsProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                for (Collection<?> col : collections) {
                    col.clear();
                }
            }
        });
    }

    private <T> void bindCheckComboBoxSelection(CheckComboBox<T> checkComboBox, final Collection<T> collection) {
        checkComboBox.getCheckModel().getCheckedItems().addListener((ListChangeListener<T>) c -> {
            while (c.next()) {
                if (c.wasAdded()) {
                    collection.addAll(c.getAddedSubList());
                } else if (c.wasRemoved()) {
                    collection.removeAll(c.getRemoved());
                }
            }
        });
    }

    private ObjectBinding<Predicate<ServerDetails>> createServerDetailsPredicateBinding() {
        return Bindings.createObjectBinding(this::buildServerDetailsPredicates,
                                            tfFilterText.textProperty(),
                                            serverFilterModel.selectedServerTagsProperty(),
                                            serverFilterModel.selectedCountriesProperty(),
                                            serverFilterModel.selectedMapsProperty(),
                                            serverFilterModel.selectedOsProperty(),
                                            serverFilterModel.selectedStatusProperty(),
                                            serverFilterModel.selectedMiscFiltersProperty(),
                                            tfSearchPlayer.textProperty()
        );
    }

    private Predicate<ServerDetails> buildServerDetailsPredicates() {
        final String searchText = StringUtils.defaultString(tfFilterText.getText(), "").toLowerCase().trim();

        Predicate<ServerDetails> textPredicate = s -> {
            if (StringUtils.isBlank(s.getName()))
                return true;
            String serverName = StringUtils.defaultString(s.getName(), "").trim().toLowerCase();
            String ipAddress = StringUtils.defaultString(s.getIpAddress(), "").toLowerCase();
            String port = s.getPort() != null ? String.valueOf(s.getPort()) : "";
            String mapName = StringUtils.defaultString(s.getMapName(), "").toLowerCase();
            String tags = StringUtils.defaultString(s.getServerTags(), "").toLowerCase();
            return serverName.contains(searchText) || ipAddress.contains(searchText) || port.contains(searchText) || mapName.contains(searchText) || tags.contains(searchText);
        };

        Predicate<ServerDetails> tagsPredicate = serverDetails -> {
            List<String> tags = GuiHelper.extractServerTags(serverDetails);
            if (serverFilterModel.getSelectedServerTags().isEmpty())
                return true;

            if (!ServerStatus.ACTIVE.equals(serverDetails.getStatus()) && !serverFilterModel.getSelectedServerTags().isEmpty())
                return false;

            if (tags.isEmpty() || serverFilterModel.getSelectedServerTags().isEmpty())
                return true;

            boolean test = true;
            for (String selectedTag : serverFilterModel.getSelectedServerTags()) {
                test = test && tags.contains(selectedTag);
            }
            return test;
        };

        Predicate<ServerDetails> countryPredicate = details -> {
            if (serverFilterModel.getSelectedCountries().isEmpty())
                return true;
            if (details.getCountry() == null && !serverFilterModel.getSelectedCountries().isEmpty())
                return false;
            return serverFilterModel.getSelectedCountries().contains(details.getCountry());
        };

        Predicate<ServerDetails> mapsPredicate = details -> {
            if (serverFilterModel.getSelectedMaps().isEmpty())
                return true;
            if (StringUtils.isBlank(details.getMapName()) && !serverFilterModel.getSelectedMaps().isEmpty())
                return false;
            return serverFilterModel.getSelectedMaps().contains(details.getMapName().toLowerCase());
        };

        Predicate<ServerDetails> osPredicate = details -> {
            if (serverFilterModel.getSelectedOs().isEmpty())
                return true;
            return serverFilterModel.getSelectedOs().contains(details.getOperatingSystem());
        };

        Predicate<ServerDetails> statusPredicate = details -> {
            if (serverFilterModel.getSelectedStatus().isEmpty())
                return true;
            return serverFilterModel.getSelectedStatus().contains(details.getStatus());
        };

        Predicate<ServerDetails> playerPredicate = details -> {
            String playerSearchText = tfSearchPlayer.getText().toLowerCase().trim();
            if (StringUtils.isBlank(tfSearchPlayer.getText()))
                return true;

            if ((details.getPlayers() == null || details.getPlayers().isEmpty()) && !StringUtils.isBlank(playerSearchText)) {
                return false;
            }
            return details.getPlayers().stream()
                    .anyMatch(p -> {
                        String playerName = StringUtils.defaultString(p.getName(), "").toLowerCase().trim();
                        return playerName.contains(playerSearchText);
                    });
        };

        Predicate<ServerDetails> miscFiltersPredicate = details -> {
            if (serverFilterModel.getSelectedMiscFilters().isEmpty())
                return true;
            boolean test = false;
            for (MiscFilters filter : serverFilterModel.getSelectedMiscFilters())
                test = test || filter.getMapper().apply(details);
            return test;
        };

        return textPredicate
                .and(tagsPredicate)
                .and(countryPredicate)
                .and(mapsPredicate)
                .and(osPredicate)
                .and(statusPredicate)
                .and(playerPredicate)
                .and(miscFiltersPredicate);
    }

    private void setupButtons() {
        //Buttons
        btnRefreshServerList.setOnAction(this::refreshServerDetails);
        btnAddServer.setOnAction(this::addServer);
        btnSetDefaultGame.setOnAction(this::updateDefaultGame);
        btnClearFilters.setOnAction(this::clearFilters);
        btnUpdateServerList.setOnAction(this::refreshServerList);
    }

    private void addServer(ActionEvent actionEvent) {
        createServerBrowserDialog("Add new server", Views.DIALOG_ADD_SERVER).show();
    }

    private JFXDialog createServerBrowserDialog(String heading, String viewName, Node... actions) {
        Region view = getViewManager().loadView(viewName);
        JFXDialog dialog = new JFXDialog();
        dialog.setTransitionType(JFXDialog.DialogTransition.TOP);
        dialog.setDialogContainer(spServerBrowser);
        JFXDialogLayout content = new JFXDialogLayout();
        content.setActions(actions);
        content.setHeading(new Label(heading));
        content.setBody(view);
        dialog.setContent(content);
        dialog.setOnDialogOpened(new EventHandler<JFXDialogEvent>() {
            @Override
            public void handle(JFXDialogEvent event) {
                TextField tf = GuiHelper.findNode(view, TextField.class, "tfIpAddress");
                if (tf != null) {
                    tf.requestFocus();
                }
            }
        });
        return dialog;
    }

    private void updateDefaultGame(ActionEvent actionEvent) {
        ConfigProfile profile = appModel.getActiveProfile();
        profile.setDefaultGame(steamGamesModel.getSelectedGame());
        getConfigService().saveProfile(profile);
    }

    private void setupSingleServerUpdateService() {
        singleUpdateService.serverDetailsProperty().bind(appModel.selectedServerProperty());
        appModel.selectedServerProperty().addListener(this::handleServerSelectionUpdate);
    }

    private void setupServerTabPanel() {
        tpServers.getSelectionModel().selectedItemProperty().addListener(this::handleTabSelectionChangeEvent);
    }

    private void handleTabSelectionChangeEvent(ObservableValue<? extends Tab> observableValue, Tab oldTab, Tab newTab) {
        appModel.setSelectedServer(null);
        if (newTab == null) {
            return;
        }
        //Load items on tab selection
        if (newTab.equals(tabBookmarks)) {
            refreshBookmarksTable();
        } else if (newTab.equals(tabManagedServers)) {
            refreshManagedServersTable();
        } else if (newTab.equals(tabServerBrowser)) {

        }
    }

    private void cancelAllUpdateTasks() {
        taskManager.getTask(UpdateMasterServerListTask.class).ifPresent(task -> task.cancel(true));
        taskManager.getTask(UpdateAllServerDetailsTask.class).ifPresent(task -> task.cancel(true));
    }

    private void refreshServerList(ActionEvent actionEvent) {
        cancelAllUpdateTasks();

        final SteamApp selectedGame = steamGamesModel.getSelectedGame();

        tvServerBrowser.itemsProperty().unbind();
        tvServerBrowser.setItems(null);

        UpdateMasterServerListTask task = springHelper.getBean(UpdateMasterServerListTask.class, selectedGame);

        btnUpdateServerList.disableProperty().bind(task.runningProperty());
        Parent placeholderView = getViewManager().getPlaceholderView(task.messageProperty(), event -> task.cancel(true));
        tvServerBrowser.setPlaceholder(placeholderView);

        taskManager.run(task)
                .whenComplete((Integer a, Throwable ex) -> {
                    if (ex != null) {
                        getNotificationManager().showError("Could not update server list: " + ex.getMessage());
                        tvServerBrowser.setPlaceholder(null);
                        return;
                    }
                    Platform.runLater(() -> {
                        refreshServerListByApp(selectedGame);
                        tvServerBrowser.setPlaceholder(null);
                        btnUpdateServerList.disableProperty().unbind();
                    });
                });
    }

    private void refreshServerDetails(ActionEvent actionEvent) {
        refreshServerListByApp(steamGamesModel.getSelectedGame());
    }

    private void refreshServerListByApp(SteamApp app) {
        if (!Platform.isFxApplicationThread())
            throw new IllegalStateException("Method should only be ran from the fx application thread");

        if (app == null)
            return;

        if (taskManager.contains(FetchServersByApp.class)) {
            return;
        }
        clearFilters(null);

        appModel.setSelectedServer(null);
        appModel.setServerListUpdating(true);
        tvServerBrowser.itemsProperty().unbind();
        appModel.setServerDetails(null);
        tvServerBrowser.setItems(null);

        Parent placeHolder = getViewManager().getPlaceholderView(String.format("Updating server list for '%s'", app.getName()));
        tvServerBrowser.setPlaceholder(placeHolder);

        FetchServersByApp fetchServersByAppTask = springHelper.getBean(FetchServersByApp.class, app);
        btnRefreshServerList.disableProperty().bind(fetchServersByAppTask.runningProperty());

        CompletableFuture<Set<ServerDetails>> fut = taskManager.run(fetchServersByAppTask);
        fut.thenAccept(servers -> {
            appModel.setServerDetails(FXCollections.observableArrayList(servers));
            tvServerBrowser.setPlaceholder(null);
            refreshServerListContent();
            appModel.setServerListUpdating(false);
        }).thenCompose(aVoid -> {
            appModel.setServerDetailsUpdating(true);
            UpdateAllServerDetailsTask updateAllDetailsTask = springHelper.getBean(UpdateAllServerDetailsTask.class, appModel.getServerDetails());
            btnRefreshServerList.disableProperty().bind(updateAllDetailsTask.runningProperty());
            return taskManager.run(updateAllDetailsTask);
        }).whenComplete((aVoid, ex) -> {
            if (ex != null) {
                if (ex instanceof CompletionException) {
                    if (ex.getCause() instanceof CancellationException) {
                        log.info("Task future has been cancelled : {}", fetchServersByAppTask);
                    }
                } else {
                    log.debug("Error during update", ex);
                }
            }
            Platform.runLater(() -> {
                btnRefreshServerList.disableProperty().unbind();
                appModel.setServerDetailsUpdating(false);
                appModel.setServerListUpdating(false);
            });

        });
    }

    private void refreshServerListContent() {
        ObjectBinding<ObservableList<ServerDetails>> filteredListBinding = createFilteredListBinding(appModel.serverDetailsProperty(), serverDetailsPredicate);
        filteredServerList.set(filteredListBinding.getValue());
        tvServerBrowser.itemsProperty().bind(filteredListBinding);
    }

    private void updateSteamAppServerEntries(ObservableValue<? extends SteamApp> observableValue, SteamApp oldApp, SteamApp newApp) {
        if (newApp != null) {
            log.info("Updating server list for app {}", newApp);
            clearFilters(null);
            refreshServerListByApp(newApp);
        } else
            getNotificationManager().showWarning("App is null");
    }

    private void refreshBookmarksTable() {
        ttvBookmarkedServers.setRoot(null);
        Parent placeholderView = getViewManager().getPlaceholderView("Loading bookmarks...");
        ttvBookmarkedServers.setPlaceholder(placeholderView);

        taskManager.run(BuildBookmarkServerTreeTask.class)
                .thenApply(GuiHelper::convertToTreeItem)
                .thenCompose(r -> {
                    Platform.runLater(() -> {
                        ttvBookmarkedServers.setRoot(r);
                        ttvBookmarkedServers.setPlaceholder(null);
                    });

                    ObservableList<ServerDetails> serverList = FXCollections.observableArrayList();
                    log.debug("Building list from tree...");
                    buildListFromTree(r, serverList);

                    UpdateAllServerDetailsTask task = springHelper.getBean(UpdateAllServerDetailsTask.class, serverList);
                    if (taskManager.contains(task)) {
                        log.debug("refreshBookmarksTable() :: An existing task is already running. Skipping.");
                        return CompletableFuture.completedFuture(null);
                    }

                    return taskManager.run(task);
                });
    }

    private void refreshManagedServersTable() {
        log.debug("Refreshing managed servers table");
        ttvManagedServers.setRoot(null);
        Parent placeholderView = getViewManager().getPlaceholderView("Loading managed servers...");
        ttvManagedServers.setPlaceholder(placeholderView);

        taskManager.run(BuildManagedServersTreeTask.class)
                .thenApply(GuiHelper::convertToTreeItem)
                .thenCompose(r -> {
                    Platform.runLater(() -> {
                        ttvManagedServers.setRoot(r);
                        ttvManagedServers.setPlaceholder(null);
                    });

                    ObservableList<ServerDetails> serverList = FXCollections.observableArrayList();
                    buildListFromTree(r, serverList);

                    UpdateAllServerDetailsTask task = springHelper.getBean(UpdateAllServerDetailsTask.class, serverList);
                    if (taskManager.contains(task)) {
                        log.debug("refreshManagedServersTable() :: An existing task is already running. Skipping.");
                        return CompletableFuture.completedFuture(null);
                    }
                    return taskManager.run(task);
                });
    }

    private void buildListFromTree(TreeItem<ServerDetails> root, List<ServerDetails> servers) {
        for (TreeItem<ServerDetails> child : root.getChildren()) {
            if (child.getValue() != null && child.getValue().getIpAddress() != null)
                servers.add(child.getValue());
            if (child.getChildren().size() > 0) {
                buildListFromTree(child, servers);
            }
        }
    }

    private void setupServerBrowserTable() {
        tvServerBrowser.getColumns().clear();
        tvServerBrowser.setRowFactory(param -> new HighlightRow<>(p -> ServerStatus.TIMED_OUT.equals(p.getStatus()), "timeout"));
        tvServerBrowser.setPlaceholder(new Label(""));

        Bindings.bindContent(appModel.getSelectedServers(), tvServerBrowser.getSelectionModel().getSelectedItems());

        createBasicColumn(tvServerBrowser, "Bookmark", "bookmarked", serverBrowserTableCellFactory::bookmark);
        createBasicColumn(tvServerBrowser, "Server Name", "name", serverBrowserTableCellFactory::serverName);
        createBasicColumn(tvServerBrowser, "IP Address", "ipAddress");
        createBasicColumn(tvServerBrowser, "Port", "port");
        createBasicColumn(tvServerBrowser, "Player Count", "playerCount");
        createBasicColumn(tvServerBrowser, "Max Players", "maxPlayerCount");
        createBasicColumn(tvServerBrowser, "Current Map", "mapName");
        createBasicColumn(tvServerBrowser, "Game", "steamApp", serverBrowserTableCellFactory::steamApp);
        createBasicColumn(tvServerBrowser, "Status", "status", serverBrowserTableCellFactory::statusInd);
        createBasicColumn(tvServerBrowser, "Country", "country", serverBrowserTableCellFactory::country);
        createBasicColumn(tvServerBrowser, "OS", "operatingSystem", serverBrowserTableCellFactory::operatingSystem);
        createBasicColumn(tvServerBrowser, "Update Date", "updateDate");
        createBasicColumn(tvServerBrowser, "Tags", "serverTags", serverBrowserTableCellFactory::tags).setPrefWidth(200);

        tvServerBrowser.setContextMenu(buildServerBrowserContextMenu());

        //Raise the following events on item selection
        tvServerBrowser.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        tvServerBrowser.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> appModel.setSelectedServer(newValue));
    }

    private ContextMenu buildServerBrowserContextMenu() {
        ContextMenu cMenu = new ContextMenu();

        MenuItem miManagedServer = new MenuItem();
        miManagedServer.textProperty().bind(Bindings.createStringBinding(() -> {
            ServerDetails selected = tvServerBrowser.getSelectionModel().getSelectedItem();
            int totalSelected = tvServerBrowser.getSelectionModel().getSelectedItems().size();
            if (serverManager.isManaged(selected)) {
                if (totalSelected > 1) {
                    return "Remove managed server(s)";
                }
                return "Remove managed server";
            }
            if (totalSelected > 1) {
                return "Add managed server(s)";
            }
            return "Add managed server";
        }, tvServerBrowser.getSelectionModel().selectedItemProperty()));
        miManagedServer.setOnAction(event -> {
            for (ServerDetails serverDetails : tvServerBrowser.getSelectionModel().getSelectedItems()) {
                if (!serverManager.isManaged(serverDetails)) {
                    log.debug("Adding managed server: {}", serverDetails);
                    serverManager.addServer(serverDetails);
                } else {
                    log.debug("Removing managed server: {}", serverDetails);
                    serverManager.removeServer(serverDetails);
                }
            }
        });

        cMenu.getItems().add(miManagedServer);
        return cMenu;
    }

    private void setupBookmarksTable() {
        ttvBookmarkedServers.getColumns().clear();
        ttvBookmarkedServers.setShowRoot(false);
        //ttvBookmarkedServers.setRowFactory(param -> new HighlightRow<>(p -> ServerStatus.TIMED_OUT.equals(p.getStatus()), "timeout"));
        TreeTableColumn<ServerDetails, String> nameCol = createBasicTreeColumn(ttvBookmarkedServers, "Server Name", "name", bookmarksTreeTableViewFactory::serverName);
        createBasicTreeColumn(ttvBookmarkedServers, "IP Address", "ipAddress", bookmarksTreeTableViewFactory);
        createBasicTreeColumn(ttvBookmarkedServers, "Port", "port", bookmarksTreeTableViewFactory);
        createBasicTreeColumn(ttvBookmarkedServers, "Player Count", "playerCount", bookmarksTreeTableViewFactory);
        createBasicTreeColumn(ttvBookmarkedServers, "Max Players", "maxPlayerCount", bookmarksTreeTableViewFactory);
        createBasicTreeColumn(ttvBookmarkedServers, "Current Map", "mapName", bookmarksTreeTableViewFactory);
        createBasicTreeColumn(ttvBookmarkedServers, "Status", "status", bookmarksTreeTableViewFactory::statusIndicator);
        createBasicTreeColumn(ttvBookmarkedServers, "Country", "country", bookmarksTreeTableViewFactory::country);
        createBasicTreeColumn(ttvBookmarkedServers, "Tags", "serverTags", bookmarksTreeTableViewFactory::serverTags);

        ttvBookmarkedServers.setTreeColumn(nameCol);
        ttvBookmarkedServers.getSelectionModel().selectedItemProperty().addListener(this::updateServerSelection);
    }

    private void setupManagedServersTable() {
        ttvManagedServers.getColumns().clear();
        ttvManagedServers.setShowRoot(false);

        TreeTableColumn<ServerDetails, String> nameCol = createBasicTreeColumn(ttvManagedServers, "Server Name", "name", param -> new FormattedTreeTableCell<>("root-col", p -> StringUtils.isBlank(p.getIpAddress())));
        createBasicTreeColumn(ttvManagedServers, "IP Address", "ipAddress", bookmarksTreeTableViewFactory);
        createBasicTreeColumn(ttvManagedServers, "Port", "port", bookmarksTreeTableViewFactory);
        createBasicTreeColumn(ttvManagedServers, "Player Count", "playerCount", bookmarksTreeTableViewFactory);
        createBasicTreeColumn(ttvManagedServers, "Max Players", "maxPlayerCount", bookmarksTreeTableViewFactory);
        createBasicTreeColumn(ttvManagedServers, "Current Map", "mapName", bookmarksTreeTableViewFactory);
        //createBasicTreeColumn(ttvManagedServers, "Game", "steamApp", bookmarksTreeTableViewFactory);
        createBasicTreeColumn(ttvManagedServers, "Status", "status", bookmarksTreeTableViewFactory::statusIndicator);
        createBasicTreeColumn(ttvManagedServers, "Country", "country", bookmarksTreeTableViewFactory::country);
        createBasicTreeColumn(ttvManagedServers, "OS", "operatingSystem", bookmarksTreeTableViewFactory::operatingSystem);
        createBasicTreeColumn(ttvManagedServers, "Tags", "serverTags", bookmarksTreeTableViewFactory::serverTags);

        ttvManagedServers.setTreeColumn(nameCol);
        ttvManagedServers.getSelectionModel().selectedItemProperty().addListener(this::updateServerSelection);
    }

    private void handleServerSelectionUpdate(ObservableValue<? extends ServerDetails> observableValue, ServerDetails oldValue, ServerDetails newValue) {
        if (newValue == null) {
            singleUpdateService.cancel();
        } else {
            if (!singleUpdateService.isRunning()) {
                singleUpdateService.restart();
            }
        }
    }

    private void updateServerSelection(ObservableValue<? extends TreeItem<ServerDetails>> observableValue, TreeItem<ServerDetails> oldValue, TreeItem<ServerDetails> newValue) {
        if (newValue != null) {
            appModel.setSelectedServer(newValue.getValue());
        } else {
            appModel.setSelectedServer(null);
        }
    }

    @Autowired
    public void setAppModel(AppModel appModel) {
        this.appModel = appModel;
    }

    @Autowired
    public void setServerBrowserTableCellFactory(ServerBrowserTableViewFactory serverBrowserTableCellFactory) {
        this.serverBrowserTableCellFactory = serverBrowserTableCellFactory;
    }

    @Autowired
    public void setSteamGamesModel(SteamAppsModel steamGamesModel) {
        this.steamGamesModel = steamGamesModel;
    }

    @Autowired
    public void setBookmarksTreeTableViewFactory(BookmarksTreeTableCellFactory bookmarksTreeTableViewFactory) {
        this.bookmarksTreeTableViewFactory = bookmarksTreeTableViewFactory;
    }

    @Autowired
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public void setSingleUpdateService(SingleServerDetailsRefreshService singleUpdateService) {
        this.singleUpdateService = singleUpdateService;
    }

    @Autowired
    public void setServerManager(ServerManager serverManager) {
        this.serverManager = serverManager;
    }

    @Autowired
    public void setServerFilterModel(ServerFilterModel serverFilterModel) {
        this.serverFilterModel = serverFilterModel;
    }
}
