package com.ibasco.sourcebuddy.controllers;

import static com.ibasco.sourcebuddy.components.GuiHelper.createBasicColumn;
import com.ibasco.sourcebuddy.components.rcon.SourceModCommand;
import com.ibasco.sourcebuddy.components.rcon.SourceModCvar;
import com.ibasco.sourcebuddy.components.rcon.SourceModExtension;
import com.ibasco.sourcebuddy.components.rcon.SourceModPlugin;
import com.ibasco.sourcebuddy.domain.KeyValueInfo;
import com.ibasco.sourcebuddy.model.AppModel;
import com.ibasco.sourcebuddy.service.SourceServerManager;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

public class SourceModController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(SourceModController.class);

    private SourceServerManager serverManager;

    @FXML
    private Button btnRefreshSMPlugins;

    @FXML
    private TableView<SourceModPlugin> tvSourcemodPlugins;

    @FXML
    private TableView<KeyValueInfo> tvSMPluginInfo;

    @FXML
    private TableView<SourceModCvar> tvSMPluginCvars;

    @FXML
    private TableView<SourceModCommand> tvSMPluginCommands;

    @FXML
    private Button btnSMExtsRefresh;

    @FXML
    private Button btnSMExtLoad;

    @FXML
    private Button tvSMExtReload;

    @FXML
    private Button btnSMExtUnload;

    @FXML
    private TableView<SourceModExtension> tvSMExtensions;

    @FXML
    private TableView<KeyValueInfo> tvSMExtensionInfo;

    private AppModel appModel;

    private ListProperty<SourceModPlugin> plugins = new SimpleListProperty<>(FXCollections.observableArrayList());

    private ObjectProperty<SourceModPlugin> selectedPlugin = new SimpleObjectProperty<>();

    @Override
    public void initialize(Stage stage, Node rootNode) {
        tvSourcemodPlugins.getColumns().clear();
        createBasicColumn(tvSourcemodPlugins, "Index", "index");
        createBasicColumn(tvSourcemodPlugins, "Name", "name");
        createBasicColumn(tvSourcemodPlugins, "Version", "version");
        createBasicColumn(tvSourcemodPlugins, "Author", "author");

        tvSMPluginInfo.getColumns().clear();
        createBasicColumn(tvSMPluginInfo, "Property", "key");
        createBasicColumn(tvSMPluginInfo, "Value", "value");

        tvSMPluginCvars.getColumns().clear();
        createBasicColumn(tvSMPluginCvars, "Name", "name");
        createBasicColumn(tvSMPluginCvars, "Value", "value");
        createBasicColumn(tvSMPluginCvars, "Types", "types");
        createBasicColumn(tvSMPluginCvars, "Description", "description");

        tvSMPluginCommands.getColumns().clear();
        createBasicColumn(tvSMPluginCommands, "Name", "name");
        createBasicColumn(tvSMPluginCommands, "Type", "type");
        createBasicColumn(tvSMPluginCommands, "Description", "description");

        tvSMExtensions.getColumns().clear();

        tvSourcemodPlugins.itemsProperty().bindBidirectional(plugins);
        bindSelectedItemBiDirectional(selectedPlugin, tvSourcemodPlugins);

        selectedPlugin.addListener(this::updateTablesOnSelection);
        btnRefreshSMPlugins.setOnAction(this::refreshSourcemodPlugins);
    }

    private void updateTablesOnSelection(ObservableValue<? extends SourceModPlugin> observable, SourceModPlugin oldValue, SourceModPlugin newValue) {
        if (newValue != null) {
            /*updatePluginInfoTable(newValue);
            updatePluginCvarsTable(newValue);
            updatePluginCommandsTable(newValue);*/
            updatePluginDetails(newValue);
        }
    }

    private void updatePluginDetails(SourceModPlugin plugin) {
        log.info("Updating plugin details");
        tvSMPluginInfo.setDisable(true);
        this.updatePluginInfoTable(plugin)
                .thenCompose(this::updatePluginCvarsTable)
                .thenCompose(this::updatePluginCommandsTable)
                .whenComplete((plugin1, ex) -> {
                    if (ex != null) {
                        log.error("updatePluginDetails() :: Complete", ex);
                    }
                    log.info("updatePluginDetails() :: Complete: {}", plugin1);
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            tvSMPluginInfo.setDisable(false);
                        }
                    });
                });
    }

    private CompletableFuture<SourceModPlugin> updatePluginInfoTable(SourceModPlugin plugin) {
        if (plugin == null)
            return CompletableFuture.failedFuture(new IllegalArgumentException("Sourcemod plugin not specified"));
        log.info("Updating plugin info: {}", plugin);
        return serverManager.updatePluginInfo(plugin)
                .thenApply(updatedPlugin -> runAndUpdateUI(updatedPlugin, c -> {
                    ObservableList<KeyValueInfo> keyValueList = FXCollections.observableArrayList();
                    keyValueList.add(new KeyValueInfo("Index", c.getIndex()));
                    keyValueList.add(new KeyValueInfo("Name", c.getName()));
                    keyValueList.add(new KeyValueInfo("Full Name", c.getFullName()));
                    keyValueList.add(new KeyValueInfo("Author", c.getAuthor()));
                    keyValueList.add(new KeyValueInfo("Version", c.getVersion()));
                    keyValueList.add(new KeyValueInfo("Filename", c.getFilename()));
                    keyValueList.add(new KeyValueInfo("Status", c.getStatus()));
                    keyValueList.add(new KeyValueInfo("Hash", c.getHash()));
                    keyValueList.add(new KeyValueInfo("URL", c.getUrl()));
                    keyValueList.add(new KeyValueInfo("Disabled", c.isDisabled()));
                    keyValueList.add(new KeyValueInfo("Timestamp", c.getTimestamp()));
                    tvSMPluginInfo.setItems(keyValueList);
                }, updatedPlugin));
    }

    private CompletableFuture<SourceModPlugin> updatePluginCvarsTable(SourceModPlugin plugin) {
        log.info("Updating plugin cvars: {}", plugin);
        return serverManager.getPluginCvars(plugin)
                .thenApply(cvars -> runAndUpdateUI(plugin, c -> tvSMPluginCvars.setItems(FXCollections.observableArrayList(c)), cvars));
    }

    private CompletableFuture<SourceModPlugin> updatePluginCommandsTable(SourceModPlugin plugin) {
        log.info("Updating plugin commands: {}", plugin);
        return serverManager.getCommands(plugin)
                .thenApply(sourceModCommands -> runAndUpdateUI(plugin, c -> tvSMPluginCommands.setItems(FXCollections.observableArrayList(c)), sourceModCommands));
    }

    private <S, T> S runAndUpdateUI(S returnType, Consumer<T> action, T arg) {
        if (Platform.isFxApplicationThread()) {
            action.accept(arg);
            return returnType;
        }
        try {
            CountDownLatch latch = new CountDownLatch(1);
            Runnable runnable = () -> {
                try {
                    action.accept(arg);
                } finally {
                    latch.countDown();
                }
            };
            Platform.runLater(runnable);
            latch.await();
        } catch (InterruptedException e) {
            throw new CompletionException(e);
        }
        return returnType;
    }

    private <T> void bindSelectedItemBiDirectional(Property<T> property, TableView<T> tableView) {
        tableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                property.setValue(newValue);
            }
        });
        property.addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                tableView.getSelectionModel().select(newValue);
            }
        });
    }

    private void refreshSourcemodPlugins(ActionEvent actionEvent) {
        btnRefreshSMPlugins.setDisable(true);
        serverManager.getPlugins(appModel.getSelectedManagedServer()).whenComplete((sourceModPlugins, ex) -> {
            try {
                if (ex != null || sourceModPlugins == null) {
                    return;
                }
                Platform.runLater(() -> plugins.setAll(sourceModPlugins));
            } finally {
                Platform.runLater(() -> btnRefreshSMPlugins.setDisable(false));
            }
        });
    }

    @Autowired
    public void setServerManager(SourceServerManager serverManager) {
        this.serverManager = serverManager;
    }

    @Autowired
    public void setAppModel(AppModel appModel) {
        this.appModel = appModel;
    }
}
