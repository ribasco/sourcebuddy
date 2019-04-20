package com.ibasco.sourcebuddy.controllers;

import com.ibasco.sourcebuddy.components.GuiHelper;
import static com.ibasco.sourcebuddy.components.GuiHelper.createBasicColumn;
import com.ibasco.sourcebuddy.controllers.fragments.AppDetailsController;
import com.ibasco.sourcebuddy.domain.SteamApp;
import com.ibasco.sourcebuddy.domain.SteamAppDetails;
import com.ibasco.sourcebuddy.gui.tableview.cells.BookmarkTableCell;
import com.ibasco.sourcebuddy.gui.tableview.cells.SteamAppDetailsCell;
import com.ibasco.sourcebuddy.gui.tableview.cells.SteamAppTableCell;
import com.ibasco.sourcebuddy.model.SteamGamesModel;
import com.ibasco.sourcebuddy.service.AppService;
import com.ibasco.sourcebuddy.service.SourceServerService;
import com.ibasco.sourcebuddy.service.SteamService;
import javafx.beans.value.ObservableValue;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;
import java.util.function.Predicate;

public class GameBrowserController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(GameBrowserController.class);

    //<editor-fold desc="FXML Properties">
    @FXML
    private TableView<SteamApp> tvGameBrowser;

    @FXML
    private ToolBar tbGameFilter;
    //</editor-fold>

    private SteamService steamQueryService;

    private SourceServerService sourceServerQueryService;

    private SteamGamesModel steamGameModel;

    private GuiHelper guiHelper;

    private FilteredList<SteamApp> filteredSteamApps;

    private AppService appService;

    @FXML
    private TextField tfGameFilter;

    @Override
    public void initialize(Stage stage, Node rootNode) {
        log.debug("Game browser initialized");
        setupGameBrowserTable();

        filteredSteamApps = new FilteredList<>(steamGameModel.steamAppListProperty());
        tvGameBrowser.setItems(filteredSteamApps);
        tfGameFilter.textProperty().addListener(this::updateFilterOnTextChange);
        guiHelper.setupToggableToolbar(tbGameFilter, tvGameBrowser);
    }

    private Runnable applyFilterCallback = new Runnable() {
        @Override
        public void run() {
            String newValue = tfGameFilter.getText();
            Predicate<SteamApp> predicate = p -> true;
            if (!StringUtils.isBlank(newValue) && newValue.length() >= 3) {
                if (StringUtils.isNumeric(newValue)) {
                    predicate = p -> p.getId().equals(Integer.valueOf(newValue));
                } else {
                    predicate = p -> p.getName().toLowerCase().contains(newValue.toLowerCase());
                }
            }
            filteredSteamApps.setPredicate(predicate);
            activated = false;
        }
    };

    private boolean activated = false;

    private void updateFilterOnTextChange(ObservableValue<? extends String> observableValue, String oldValue, String newValue) {
        log.debug("Filter changed: {}", newValue);
        if (!activated) {
            appService.runTaskAfter(Duration.ofMillis(1000), applyFilterCallback);
            activated = true;
            log.debug("Activated");
            return;
        }
        log.debug("Touchinig task");
        appService.touchTask(applyFilterCallback);
    }

    private void setupGameBrowserTable() {
        tvGameBrowser.getColumns().clear();

        double minWidth = 230;

        TableColumn<SteamApp, Integer> col02 = createBasicColumn(tvGameBrowser, "Thumbnail", "id", this::drawSteamAppCell);
        col02.setMinWidth(minWidth);
        col02.setMaxWidth(minWidth);

        TableColumn<SteamApp, SteamAppDetails> col03 = createBasicColumn(tvGameBrowser, "Description", "appDetails", this::drawAppDetailsCell, false);
        col03.setPrefWidth(minWidth * 2);
        tvGameBrowser.widthProperty().addListener((observable, oldValue, newValue) -> {
            Double newWidth = (Double) newValue;
            if (newWidth == null)
                return;
            if (newWidth > (minWidth * 2)) {
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
                steamQueryService.updateBookmarkFlag(app);
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
            steamQueryService.updateBookmarkFlag(steamApp, booleanProperty.getValue());
        });
    }

    @Autowired
    public void setSteamQueryService(SteamService steamQueryService) {
        this.steamQueryService = steamQueryService;
    }

    @Autowired
    public void setSourceServerQueryService(SourceServerService sourceServerQueryService) {
        this.sourceServerQueryService = sourceServerQueryService;
    }

    @Autowired
    public void setSteamGameModel(SteamGamesModel steamGameModel) {
        this.steamGameModel = steamGameModel;
    }

    @Autowired
    public void setGuiHelper(GuiHelper guiHelper) {
        this.guiHelper = guiHelper;
    }

    @Autowired
    public void setAppService(AppService appService) {
        this.appService = appService;
    }
}
