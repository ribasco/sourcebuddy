package com.ibasco.sourcebuddy.controllers;

import com.ibasco.sourcebuddy.components.GuiHelper;
import static com.ibasco.sourcebuddy.components.GuiHelper.*;
import com.ibasco.sourcebuddy.constants.Views;
import com.ibasco.sourcebuddy.domain.ConfigProfile;
import com.ibasco.sourcebuddy.domain.Country;
import com.ibasco.sourcebuddy.domain.ServerDetails;
import com.ibasco.sourcebuddy.domain.SteamApp;
import com.ibasco.sourcebuddy.enums.MiscFilters;
import com.ibasco.sourcebuddy.enums.OperatingSystem;
import com.ibasco.sourcebuddy.enums.ServerStatus;
import com.ibasco.sourcebuddy.gui.converters.MappedObjectStringConverter;
import com.ibasco.sourcebuddy.gui.tableview.factory.ServerBrowserTableViewFactory;
import com.ibasco.sourcebuddy.gui.tableview.rows.HighlightRow;
import com.ibasco.sourcebuddy.gui.treetableview.cells.FormattedTreeTableCell;
import com.ibasco.sourcebuddy.gui.treetableview.factory.BookmarksTreeTableCellFactory;
import com.ibasco.sourcebuddy.model.ServerDetailsModel;
import com.ibasco.sourcebuddy.model.ServerFilterModel;
import com.ibasco.sourcebuddy.model.SteamAppsModel;
import com.ibasco.sourcebuddy.service.ServerManager;
import com.ibasco.sourcebuddy.service.impl.SingleServerDetailsRefreshService;
import com.ibasco.sourcebuddy.tasks.*;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialogLayout;
import com.jfoenix.controls.JFXProgressBar;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
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
import java.util.concurrent.CompletableFuture;
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
    private JFXProgressBar pbServerLoad;

    @FXML
    private TabPane tpServers;

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
    private CheckComboBox<String> cbFilterTags;

    @FXML
    private CheckComboBox<Country> cbFilterCountry;

    @FXML
    private CheckComboBox<String> cbFilterMaps;

    @FXML
    private CheckComboBox<ServerStatus> cbFilterStatus;

    @FXML
    private Button btnUpdateServerList;

    @FXML
    private CheckComboBox<OperatingSystem> cbFilterOs;

    @FXML
    private Label lblSelectedGame;

    @FXML
    private TextField tfSearchPlayer;

    @FXML
    private Button btnClearFilters;

    @FXML
    private CheckComboBox<MiscFilters> cbFilterMisc;
    //</editor-fold>

    private ServerDetailsModel serverDetailsModel;

    private SteamAppsModel steamGamesModel;

    private BookmarksTreeTableCellFactory bookmarksTreeTableViewFactory;

    private ServerBrowserTableViewFactory serverBrowserTableCellFactory;

    private SingleServerDetailsRefreshService singleUpdateService;

    private ServerManager serverManager;

    private ObjectProperty<Predicate<ServerDetails>> serverDetailsPredicate = new SimpleObjectProperty<>(p -> true);

    private ServerFilterModel serverFilterModel;

    private ListProperty<ServerDetails> filteredServerList = new SimpleListProperty<>();

    @Override
    public void initialize(Stage stage, Node rootNode) {
        setupServerTabPanel();
        setupFilterComponents();
        setupServerBrowserTable();
        setupBookmarksTable();
        setupManagedServersTable();
        setupSingleServerUpdateService();
        setupButtons();

        steamGamesModel.selectedGameProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null)
                clearFilters(null);
        });

        updateServerListContent();

        lblSelectedGame.textProperty().bind(Bindings.format("%s (Total: %d)", steamGamesModel.selectedGameProperty(), filteredServerList.sizeProperty()));
        tpServers.setTabDragPolicy(TabPane.TabDragPolicy.REORDER);
        pbServerLoad.visibleProperty().bind(serverDetailsModel.serverListUpdatingProperty());
        steamGamesModel.selectedGameProperty().addListener(this::updateSteamAppServerEntries);
        steamGamesModel.setSelectedGame(serverDetailsModel.getActiveProfile().getDefaultGame());
        btnUpdateServerList.setOnAction(this::updateServerList);
        btnClearFilters.setOnAction(this::clearFilters);
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

    private void updateServerList(ActionEvent actionEvent) {
        if (taskManager.contains(UpdateMasterServerListTask.class)) {
            log.warn("Task already running. Skipping");
            return;
        }
        final SteamApp selectedGame = steamGamesModel.getSelectedGame();
        UpdateMasterServerListTask task = springHelper.getBean(UpdateMasterServerListTask.class, selectedGame);
        taskManager.run(task)
                .whenComplete((aVoid, ex) -> {
                    if (ex != null) {
                        getNotificationManager().showError("Could not update server list: " + ex.getMessage());
                        return;
                    }
                    Platform.runLater(() -> refreshServerListByApp(selectedGame));
                });
    }

    private void setupFilterComponents() {
        serverDetailsPredicate.bind(createServerDetailsPredicateBinding());

        clearOnNewListSelection(serverFilterModel.getServerTags(), serverFilterModel.getCountries(), serverFilterModel.getMaps());

        serverDetailsModel.serverDetailsProperty().addListener((ListChangeListener<ServerDetails>) c -> {
            while (c.next()) {
                if (c.wasUpdated()) {
                    for (int i = c.getFrom(); i < c.getTo(); ++i) {
                        ServerDetails details = serverDetailsModel.getServerDetails().get(i);
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
        GuiHelper.bindContent(cbFilterTags.getItems(), serverFilterModel.getServerTags());
        GuiHelper.bindContent(cbFilterCountry.getItems(), serverFilterModel.getCountries());
        GuiHelper.bindContent(cbFilterMaps.getItems(), serverFilterModel.getMaps());
        GuiHelper.bindContent(cbFilterStatus.getItems(), FXCollections.observableSet(ServerStatus.values()));
        GuiHelper.bindContent(cbFilterOs.getItems(), FXCollections.observableSet(OperatingSystem.values()));
        GuiHelper.bindContent(cbFilterMisc.getItems(), FXCollections.observableSet(MiscFilters.values()));
    }

    private <T> void initializeFilterCheckComboBox(CheckComboBox<T> comboBox, T[] values, Function<T, String> labelMapper) {
        comboBox.setConverter(new MappedObjectStringConverter<>(labelMapper));
    }

    private void clearOnNewListSelection(Collection<?>... collections) {
        serverDetailsModel.serverDetailsProperty().addListener((observable, oldValue, newValue) -> {
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
        return Bindings.createObjectBinding(() -> {
                                                String searchText = StringUtils.defaultString(tfFilterText.getText(), "").toLowerCase().trim();

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
                                                    return serverFilterModel.getSelectedMaps().contains(details.getMapName());
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
                                                    boolean test = true;
                                                    for (MiscFilters filter : serverFilterModel.getSelectedMiscFilters())
                                                        test = test && filter.getMapper().apply(details);
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
                                            },
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

    private void setupButtons() {
        //Buttons
        btnRefreshServerList.setOnAction(this::refreshServerList);
        btnAddServer.setOnAction(this::addServer);
        btnSetDefaultGame.setOnAction(this::setDefaultGame);
    }

    private void refreshServerList(ActionEvent actionEvent) {
        refreshServerListByApp(steamGamesModel.getSelectedGame());
    }

    private void addServer(ActionEvent actionEvent) {
        createServerBrowserDialog("Add new server", Views.DIALOG_ADD_SERVER).show();
    }

    private JFXDialog createServerBrowserDialog(String heading, String viewName, Node... actions) {
        Region view = getViewManager().loadView(viewName);
        JFXDialog dialog = new JFXDialog();
        dialog.setDialogContainer(spServerBrowser);
        JFXDialogLayout content = new JFXDialogLayout();
        content.setActions(actions);
        content.setHeading(new Label(heading));
        content.setBody(view);
        dialog.setContent(content);
        return dialog;
    }

    private void setDefaultGame(ActionEvent actionEvent) {
        ConfigProfile profile = serverDetailsModel.getActiveProfile();
        profile.setDefaultGame(steamGamesModel.getSelectedGame());
        getConfigService().saveProfile(profile);
    }

    private void setupSingleServerUpdateService() {
        singleUpdateService.serverDetailsProperty().bind(serverDetailsModel.selectedServerProperty());
        serverDetailsModel.selectedServerProperty().addListener(this::handleServerSelectionUpdate);
    }

    private void setupServerTabPanel() {
        tpServers.getSelectionModel().selectedItemProperty().addListener(this::handleTabSelectionChangeEvent);
    }

    private void handleTabSelectionChangeEvent(ObservableValue<? extends Tab> observableValue, Tab oldTab, Tab newTab) {
        serverDetailsModel.setSelectedServer(null);
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

    private void refreshServerListByApp(SteamApp app) {
        if (!Platform.isFxApplicationThread())
            throw new IllegalStateException("Method should only be ran from the fx application thread");

        if (app == null)
            return;

        if (taskManager.contains(FetchServersByApp.class) ||
                serverDetailsModel.isServerDetailsUpdating() ||
                serverDetailsModel.isServerListUpdating()) {
            return;
        }

        clearFilters(null);

        serverDetailsModel.setSelectedServer(null);
        serverDetailsModel.setServerListUpdating(true);
        tvServerBrowser.itemsProperty().unbind();
        serverDetailsModel.setServerDetails(null);
        tvServerBrowser.setItems(null);

        Label placeHolder = new Label();
        placeHolder.setText(String.format("Updating server list for '%s'", app.getName()));
        tvServerBrowser.setPlaceholder(placeHolder);

        FetchServersByApp task = springHelper.getBean(FetchServersByApp.class, app);
        CompletableFuture<List<ServerDetails>> fut = taskManager.run(task);
        fut.thenAccept(servers -> {
            serverDetailsModel.setServerDetails(FXCollections.observableArrayList(servers));
            updateServerListContent();
            serverDetailsModel.setServerListUpdating(false);
        }).thenCompose(aVoid -> {
            serverDetailsModel.setServerDetailsUpdating(true);
            return taskManager.run(UpdateAllServerDetailsTask.class, serverDetailsModel.getServerDetails());
        }).whenComplete((aVoid, ex) -> {
            if (ex != null) {
                //notificationManager.showError("Error occured during update %s", throwable.getMessage());
                log.debug("Error during update", ex);
            }
            serverDetailsModel.setServerDetailsUpdating(false);
            serverDetailsModel.setServerListUpdating(false);
            refreshMapFilterEntries();
        });
    }

    private void refreshMapFilterEntries() {
        for (ServerDetails details : serverDetailsModel.getServerDetails()) {
            if (!StringUtils.isBlank(details.getMapName()))
                serverFilterModel.getMaps().add(details.getMapName());
        }
    }

    private void updateServerListContent() {
        ObjectBinding<ObservableList<ServerDetails>> filteredListBinding = createFilteredListBinding(serverDetailsModel.serverDetailsProperty(), serverDetailsPredicate);
        filteredServerList.set(filteredListBinding.getValue());
        tvServerBrowser.itemsProperty().bind(filteredListBinding);
    }

    private void updateSteamAppServerEntries(ObservableValue<? extends SteamApp> observableValue, SteamApp oldApp, SteamApp newApp) {
        if (newApp != null)
            refreshServerListByApp(newApp);
    }

    private void refreshBookmarksTable() {
        ttvBookmarkedServers.setRoot(null);
        Parent placeholderView = getGuiHelper().getLoadingPlaceholder("Loading bookmarks...");
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
        Parent placeholderView = getGuiHelper().getLoadingPlaceholder("Loading managed servers...");
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

        Bindings.bindContent(serverDetailsModel.getSelectedServers(), tvServerBrowser.getSelectionModel().getSelectedItems());

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
        tvServerBrowser.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> serverDetailsModel.setSelectedServer(newValue));
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
        createBasicTreeColumn(ttvManagedServers, "Tags", "serverTags", bookmarksTreeTableViewFactory::serverTags);
        createBasicTreeColumn(ttvManagedServers, "OS", "operatingSystem", bookmarksTreeTableViewFactory::operatingSystem);

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
        if (newValue != null)
            serverDetailsModel.setSelectedServer(newValue.getValue());
        else
            serverDetailsModel.setSelectedServer(null);
    }

    @Autowired
    public void setServerDetailsModel(ServerDetailsModel serverDetailsModel) {
        this.serverDetailsModel = serverDetailsModel;
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
