package com.ibasco.sourcebuddy.controllers;

import static com.ibasco.sourcebuddy.components.GuiHelper.createBasicColumn;
import static com.ibasco.sourcebuddy.components.GuiHelper.createDecoratedTableCell;
import com.ibasco.sourcebuddy.components.rcon.SourceModCommand;
import com.ibasco.sourcebuddy.components.rcon.SourceModCvar;
import com.ibasco.sourcebuddy.components.rcon.SourceModExtension;
import com.ibasco.sourcebuddy.components.rcon.SourceModPlugin;
import com.ibasco.sourcebuddy.domain.KeyValueInfo;
import com.ibasco.sourcebuddy.gui.decorators.CellDecorator;
import com.ibasco.sourcebuddy.model.AppModel;
import com.ibasco.sourcebuddy.service.SourceServerManager;
import com.jfoenix.controls.JFXSpinner;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;
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

    @FXML
    private TitledPane tpSMPluginInfo;

    @FXML
    private TitledPane tpSMPluginCvars;

    @FXML
    private TitledPane tpSMPluginCommands;

    @Override
    public void initialize(Stage stage, Node rootNode) {
        tvSourcemodPlugins.getColumns().clear();
        createBasicColumn(tvSourcemodPlugins, "Index", "index", this::pluginIndexWithPi);
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

    public TableCell<SourceModPlugin, Integer> pluginIndexWithPi(TableColumn<SourceModPlugin, Integer> column) {
        return createDecoratedTableCell(new CellDecorator<>() {

            private Map<SourceModPlugin, JFXSpinner> piMap = new HashMap<>();

            @Override
            public void decorate(Integer item, IndexedCell<SourceModPlugin> cell) {
                TableCell tc = (TableCell) cell;
                SourceModPlugin plugin = (SourceModPlugin) tc.getTableRow().getItem();
                if (plugin == null)
                    return;
                piMap.computeIfAbsent(plugin, smp -> retrievePi(smp, tc));
                cell.setText(String.valueOf(item));
                cell.setAlignment(Pos.CENTER);
                cell.setTextAlignment(TextAlignment.CENTER);
            }

            private JFXSpinner retrievePi(SourceModPlugin plugin, TableCell cell) {
                JFXSpinner pi = new JFXSpinner();
                pi.setPrefSize(16, 16);
                plugin.updatingProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue == null)
                        return;
                    Platform.runLater(() -> {
                        if (newValue) {
                            cell.setText(null);
                            cell.setGraphic(pi);
                            pi.setVisible(true);
                        } else {
                            pi.setVisible(false);
                            cell.setText(String.valueOf(cell.getItem()));
                            cell.setGraphic(null);
                        }
                    });
                });
                return pi;
            }
        });
    }

    private void updateTablesOnSelection(ObservableValue<? extends SourceModPlugin> observable, SourceModPlugin oldValue, SourceModPlugin newValue) {
        if (oldValue != null) {
            tvSMPluginCvars.itemsProperty().unbind();
            tvSMPluginCommands.itemsProperty().unbind();
            tpSMPluginCvars.textProperty().unbind();
            tpSMPluginCommands.textProperty().unbind();
        }
        if (newValue != null) {
            //Skip if there is an existing requset on-going
            if (newValue.isUpdating())
                return;
            tvSMPluginInfo.setItems(null);
            tvSMPluginCvars.setItems(null);
            tvSMPluginCommands.setItems(null);
            newValue.setUpdating(true);
            updateAllPluginDetails(newValue).whenComplete((plugin, ex) -> {
                if (ex != null) {
                    log.error("An error occured during plugin details update: {}", ex.getMessage());
                    newValue.setUpdating(false);
                    return;
                }
                Platform.runLater(() -> {
                    try {
                        log.info("DONE");
                        SourceModPlugin selected = selectedPlugin.get();

                        //If selection has changed and the selection is not of this request, skip updating ui
                        if (selected != null && !selected.equals(plugin)) {
                            log.debug("Skipping UI Tables update {} != {}", selected, plugin);
                            return;
                        }

                        refreshPluginInfoTable(plugin);

                        tvSMPluginCvars.itemsProperty().bind(plugin.cvarsProperty());
                        tvSMPluginCommands.itemsProperty().bind(plugin.commandsProperty());
                        tpSMPluginCvars.textProperty().bind(Bindings.createStringBinding(() -> {
                            if (plugin.getCvars() != null && plugin.getCvars().size() > 0) {
                                return String.format("Cvars (%d)", plugin.getCvars().size());
                            } else {
                                return "Cvars";
                            }
                        }, plugin.cvarsProperty()));
                        tpSMPluginCommands.textProperty().bind(Bindings.createStringBinding(() -> {
                            if (plugin.getCommands() != null && plugin.getCommands().size() > 0) {
                                return String.format("Commands (%d)", plugin.getCommands().size());
                            } else {
                                return "Commands";
                            }
                        }, plugin.commandsProperty()));
                    } finally {
                        newValue.setUpdating(false);
                    }
                });
            });
        }
    }

    private void refreshPluginInfoTable(SourceModPlugin plugin) {
        ObservableList<KeyValueInfo> keyValueList = FXCollections.observableArrayList();
        keyValueList.add(new KeyValueInfo("Index", plugin.getIndex()));
        keyValueList.add(new KeyValueInfo("Name", plugin.getName()));
        keyValueList.add(new KeyValueInfo("Full Name", plugin.getFullName()));
        keyValueList.add(new KeyValueInfo("Author", plugin.getAuthor()));
        keyValueList.add(new KeyValueInfo("Version", plugin.getVersion()));
        keyValueList.add(new KeyValueInfo("Filename", plugin.getFilename()));
        keyValueList.add(new KeyValueInfo("Status", plugin.getStatus()));
        keyValueList.add(new KeyValueInfo("Hash", plugin.getHash()));
        keyValueList.add(new KeyValueInfo("URL", plugin.getUrl()));
        keyValueList.add(new KeyValueInfo("Disabled", plugin.isDisabled()));
        keyValueList.add(new KeyValueInfo("Timestamp", plugin.getTimestamp()));
        tvSMPluginInfo.setItems(keyValueList);
    }

    private CompletableFuture<SourceModPlugin> updateAllPluginDetails(SourceModPlugin plugin) {
        if (StringUtils.isNotBlank(plugin.getFilename()) && StringUtils.isNotBlank(plugin.getHash()) && StringUtils.isNotBlank(plugin.getUrl())) {
            log.info("Skipping plugin details update");
            return CompletableFuture.completedFuture(plugin);
        }
        return this.updatePluginInfo(plugin)
                .thenCompose(this::updatePluginCvarsTable)
                .thenCompose(this::updatePluginCommandsTable);
    }

    private CompletableFuture<SourceModPlugin> updatePluginInfo(SourceModPlugin plugin) {
        if (plugin == null)
            return CompletableFuture.failedFuture(new IllegalArgumentException("Sourcemod plugin not specified"));
        if (StringUtils.isNotBlank(plugin.getFilename()))
            return CompletableFuture.completedFuture(plugin);
        return serverManager.updatePluginInfo(plugin);
    }

    private CompletableFuture<SourceModPlugin> updatePluginCvarsTable(SourceModPlugin plugin) {
        if (plugin.getCvars() != null && !plugin.getCvars().isEmpty())
            return CompletableFuture.completedFuture(plugin);
        return serverManager.getPluginCvars(plugin)
                .thenApply(cvars -> runAndUpdateUI(plugin, cvarList -> plugin.setCvars(FXCollections.observableArrayList(cvarList)), cvars));
    }

    private CompletableFuture<SourceModPlugin> updatePluginCommandsTable(SourceModPlugin plugin) {
        if (plugin.getCommands() != null && !plugin.getCommands().isEmpty())
            return CompletableFuture.completedFuture(plugin);
        return serverManager.getCommands(plugin)
                .thenApply(sourceModCommands -> runAndUpdateUI(plugin, commandList -> plugin.setCommands(FXCollections.observableArrayList(commandList)), sourceModCommands));
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
