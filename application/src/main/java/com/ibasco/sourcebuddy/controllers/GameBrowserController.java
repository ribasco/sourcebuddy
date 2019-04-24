package com.ibasco.sourcebuddy.controllers;

import com.ibasco.sourcebuddy.components.GuiHelper;
import static com.ibasco.sourcebuddy.components.GuiHelper.createBasicColumn;
import com.ibasco.sourcebuddy.controllers.fragments.AppDetailsController;
import com.ibasco.sourcebuddy.domain.SteamApp;
import com.ibasco.sourcebuddy.domain.SteamAppDetails;
import com.ibasco.sourcebuddy.gui.listeners.RunnableTextChangeListener;
import com.ibasco.sourcebuddy.gui.tableview.cells.BookmarkTableCell;
import com.ibasco.sourcebuddy.gui.tableview.cells.SteamAppDetailsCell;
import com.ibasco.sourcebuddy.gui.tableview.cells.SteamAppTableCell;
import com.ibasco.sourcebuddy.model.SteamAppsModel;
import com.ibasco.sourcebuddy.service.SteamService;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.function.Predicate;

public class GameBrowserController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(GameBrowserController.class);

    private static final int MIN_WIDTH = 230;

    //<editor-fold desc="FXML Properties">
    @FXML
    private TableView<SteamApp> tvGameBrowser;

    @FXML
    private ToolBar tbGameFilter;

    @FXML
    private TextField tfGameFilter;

    @FXML
    private Button btnRefreshApps;

    @FXML
    private Button btnClearFilter;

    @FXML
    private ToggleGroup filterGroup;
    //</editor-fold>

    private SteamService steamService;

    private SteamAppsModel steamGameModel;

    private GuiHelper guiHelper;

    private ListProperty<SteamApp> filteredApps = new SimpleListProperty<>();

    private ObjectProperty<Predicate<? super SteamApp>> filterPredicate = new SimpleObjectProperty<>(p -> true);

    private Runnable applyFilterCallback = new Runnable() {
        @Override
        public void run() {
            String newValue = tfGameFilter.getText();
            Predicate<SteamApp> predicate = p -> true;
            if (!StringUtils.isBlank(newValue) && newValue.length() >= 3) {
                if (StringUtils.isNumeric(newValue)) {
                    predicate = p -> p.getId().equals(Integer.valueOf(newValue.trim()));
                } else {
                    predicate = p -> p.getName().trim().toLowerCase().contains(newValue.toLowerCase());
                }
            }
            GameBrowserController.this.filterPredicate.set(predicate);
        }
    };

    @Override
    public void initialize(Stage stage, Node rootNode) {
        log.debug("Game browser initialized");
        setupGameBrowserTable();

        filteredApps.bind(Bindings.createObjectBinding(() -> {
            if (steamGameModel.getSteamAppList() != null) {
                FilteredList<SteamApp> filteredList = steamGameModel.getSteamAppList().filtered(p -> true);
                filteredList.predicateProperty().bindBidirectional(filterPredicate);
                return filteredList;
            }
            return FXCollections.emptyObservableList();
        }, steamGameModel.steamAppListProperty()));

        //steamGameModel.steamAppListProperty().bind();
        tvGameBrowser.itemsProperty().bind(filteredApps);
        RunnableTextChangeListener filterChangeListener = springHelper.getBean(RunnableTextChangeListener.class, applyFilterCallback);
        tfGameFilter.textProperty().addListener(filterChangeListener);
        guiHelper.setupToggableToolbar(tbGameFilter, tvGameBrowser);
        btnClearFilter.setOnAction(this::clearFilter);
        btnRefreshApps.setOnAction(this::refreshApps);
    }

    private void refreshApps(ActionEvent actionEvent) {
        steamGameModel.setSteamAppList(null);
        Parent placeholderView = guiHelper.getLoadingPlaceholder("Loading steam apps...");
        tvGameBrowser.setPlaceholder(placeholderView);
        steamService.findSteamAppsFromRepo().thenApply(FXCollections::observableArrayList).whenComplete((steamApps, ex) -> {
            if (ex != null) {
                getNotificationManager().showError("Could not retrieve steam app list: " + ex.getMessage());
                return;
            }
            steamGameModel.setSteamAppList(steamApps);
        });
    }

    private void clearFilter(ActionEvent actionEvent) {
        filterPredicate.set(p -> true);
    }

    private void setupGameBrowserTable() {
        tvGameBrowser.getColumns().clear();

        TableColumn<SteamApp, Integer> col02 = createBasicColumn(tvGameBrowser, "Thumbnail", "id", this::drawSteamAppCell);
        col02.setMinWidth(MIN_WIDTH);
        col02.setMaxWidth(MIN_WIDTH);

        TableColumn<SteamApp, SteamAppDetails> col03 = createBasicColumn(tvGameBrowser, "Description", "appDetails", this::drawAppDetailsCell, false);
        col03.setPrefWidth(MIN_WIDTH * 2);
        tvGameBrowser.widthProperty().addListener((observable, oldValue, newValue) -> {
            Double newWidth = (Double) newValue;
            if (newWidth == null)
                return;
            if (newWidth > (MIN_WIDTH * 2)) {
                if (!tvGameBrowser.getColumns().contains(col03)) {
                    tvGameBrowser.getColumns().add(col03);
                    tvGameBrowser.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
                }
            } else {
                tvGameBrowser.getColumns().remove(col03);
            }
        });

        tvGameBrowser.setContextMenu(createGameBrowserCMenu());
        tvGameBrowser.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    private ContextMenu createGameBrowserCMenu() {
        ContextMenu cMenu = new ContextMenu();
        MenuItem miAddBookmark = new MenuItem("Add Game");
        miAddBookmark.setOnAction(event -> {
            SteamApp app = tvGameBrowser.getSelectionModel().getSelectedItem();
            if (app != null) {
                steamService.updateBookmarkFlag(app);
                log.debug("Added bookmark for {}", app);
            }
        });
        cMenu.getItems().add(miAddBookmark);
        return cMenu;
    }

    private TableCell<SteamApp, SteamAppDetails> drawAppDetailsCell(TableColumn<SteamApp, SteamAppDetails> abTableColumn) {
        return viewManager.loadViewFragmentCell(SteamAppDetailsCell.class, "cell-game-details", AppDetailsController.class);
    }

    private TableCell<SteamApp, Integer> drawSteamAppCell(TableColumn<SteamApp, Integer> abTableColumn) {
        return getAppContext().getBean(SteamAppTableCell.class);
    }

    private TableCell<SteamApp, Boolean> drawBookmarkNode(TableColumn<SteamApp, Boolean> abTableColumn) {
        return new BookmarkTableCell<>((steamApp, booleanProperty) -> {
            steamService.updateBookmarkFlag(steamApp, booleanProperty.getValue());
        });
    }

    @Autowired
    public void setSteamService(SteamService steamService) {
        this.steamService = steamService;
    }

    @Autowired
    public void setSteamGameModel(SteamAppsModel steamGameModel) {
        this.steamGameModel = steamGameModel;
    }

    @Autowired
    public void setGuiHelper(GuiHelper guiHelper) {
        this.guiHelper = guiHelper;
    }
}
