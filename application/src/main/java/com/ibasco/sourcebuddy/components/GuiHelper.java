package com.ibasco.sourcebuddy.components;

import com.ibasco.sourcebuddy.domain.KeyValueInfo;
import com.ibasco.sourcebuddy.service.AppService;
import com.ibasco.sourcebuddy.util.Delta;
import com.ibasco.sourcebuddy.util.WorkProgressCallback;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.controlsfx.control.MasterDetailPane;
import org.dockfx.DockNode;
import org.dockfx.DockTitleBar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Component
public class GuiHelper {

    private static final Logger log = LoggerFactory.getLogger(GuiHelper.class);

    private AppService appService;

    @SuppressWarnings("Duplicates")
    public static void moveStageOnDrag(Scene scene) {
        final Stage stage = (Stage) scene.getWindow();
        final Delta dragDelta = new Delta();

        if (stage == null) {
            scene.windowProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    Stage stage1 = (Stage) newValue;
                    scene.setOnMousePressed(event -> {
                        // record a delta distance for the drag and drop operation.
                        dragDelta.x = stage1.getX() - event.getScreenX();
                        dragDelta.y = stage1.getY() - event.getScreenY();
                    });

                    scene.setOnMouseDragged(mouseEvent -> {
                        stage1.setX(mouseEvent.getScreenX() + dragDelta.x);
                        stage1.setY(mouseEvent.getScreenY() + dragDelta.y);
                    });
                }
            });
            return;
        }

        //noinspection Duplicates
        scene.setOnMousePressed(event -> {
            // record a delta distance for the drag and drop operation.
            dragDelta.x = stage.getX() - event.getScreenX();
            dragDelta.y = stage.getY() - event.getScreenY();
        });

        scene.setOnMouseDragged(mouseEvent -> {
            stage.setX(mouseEvent.getScreenX() + dragDelta.x);
            stage.setY(mouseEvent.getScreenY() + dragDelta.y);
        });
    }

    public static void setupKeyValueTable(TableView<KeyValueInfo> tableView) {
        TableColumn<KeyValueInfo, String> nameCol = new TableColumn<>("Property");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("key"));

        TableColumn<KeyValueInfo, String> valueCol = new TableColumn<>("Value");
        valueCol.setCellValueFactory(new PropertyValueFactory<>("value"));

        //noinspection unchecked
        tableView.getColumns().addAll(nameCol, valueCol);
    }

    public static <A, B> TableColumn<A, B> createBasicColumn(TableView<A> table, String label, String propertyName) {
        return createBasicColumn(table, label, propertyName, true);
    }

    public static <A, B> TableColumn<A, B> createBasicColumn(TableView<A> table, String label, String propertyName, boolean updateTable) {
        return createBasicColumn(table, label, propertyName, null, updateTable);
    }

    public static <A, B> TableColumn<A, B> createBasicColumn(TableView<A> table, String label, String propertyName, Callback<TableColumn<A, B>, TableCell<A, B>> cellFactory) {
        return createBasicColumn(table, label, propertyName, cellFactory, true);
    }

    public static <A, B> TableColumn<A, B> createBasicColumn(TableView<A> table, String label, String propertyName, Callback<TableColumn<A, B>, TableCell<A, B>> cellFactory, boolean updateTable) {
        PropertyValueFactory<A, B> valueFactory = null;
        if (propertyName != null && !propertyName.isBlank()) {
            valueFactory = new PropertyValueFactory<>(propertyName);
        }
        return createBasicColumn(table, label, valueFactory, cellFactory, updateTable);
    }

    public static <A, B> TableColumn<A, B> createBasicColumn(TableView<A> table, String label, Callback<TableColumn.CellDataFeatures<A, B>, ObservableValue<B>> valueFactory, Callback<TableColumn<A, B>, TableCell<A, B>> cellFactory, boolean updateTable) {
        TableColumn<A, B> column = new TableColumn<>(label);
        if (valueFactory != null)
            column.setCellValueFactory(valueFactory);
        if (cellFactory != null) {
            column.setCellFactory(cellFactory);
        }
        if (updateTable)
            table.getColumns().add(column);
        return column;
    }

    public static <T, U> TableCell<T, U> createTableCell(BiConsumer<U, TableCell<T, U>> callback) {
        return new TableCell<>() {
            @Override
            protected void updateItem(U item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                } else {
                    callback.accept(item, this);
                }
            }
        };
    }

    public static <T> int mergeList(final List<T> source, List<T> target) {
        return mergeList(source, target, null);
    }

    public static <T> int mergeList(final List<T> source, List<T> target, WorkProgressCallback<T> callback) {
        int count = 0;
        for (T svr : source) {
            if (svr != null && !target.contains(svr)) {
                target.add(svr);
                count++;
                invokeIfPresent(callback, svr, null);
            }
        }
        return count;
    }

    public static <T> void invokeIfPresent(WorkProgressCallback<T> callback, T target, Throwable ex) {
        if (callback != null) {
            callback.onProgress(target, ex);
        }
    }

    public static void updateOrientationOnResize(MasterDetailPane pane, double threshold) {
        if (pane == null)
            return;
        pane.widthProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue.doubleValue() > threshold) {
                pane.setDetailSide(Side.RIGHT);
            } else {
                pane.setDetailSide(Side.BOTTOM);
            }
        });
    }

    public static void hideDetailPaneOnHeightChange(MasterDetailPane pane, double threshold) {
        if (pane == null)
            return;
        pane.heightProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue.doubleValue() < threshold) {
                pane.setShowDetailNode(false);
            } else {
                pane.setShowDetailNode(true);
            }
        });
    }

    public static void findNode(Parent node, Consumer<Node> callback) {
        findNode(node, null, callback);
    }

    public static <T> T findNode(Parent node, Class<T> find) {
        return findNode(node, find, 0, null);
    }

    public static <T> T findNode(Parent node, Class<T> find, Consumer<Node> callback) {
        return findNode(node, find, 0, callback);
    }

    public static <T> T findNode(Parent parent, Class<T> find, int level, Consumer<Node> callback) {
        for (Node child : parent.getChildrenUnmodifiable()) {
            if (callback != null) {
                callback.accept(child);
            }
            if (find != null) {
                if (child.getClass().isAssignableFrom(find))
                    //noinspection unchecked
                    return (T) child;
            }
            if (child instanceof Parent) {
                findNode((Parent) child, find, level + 1, callback);
            }
        }
        return null;
    }

    public static void printDockHierarchy(ObservableList<Node> items, int level) {
        for (var item : items) {
            String parent = item.getParent() == null ? "N/A" : item.getParent().getClass().getSimpleName();
            if (item instanceof SplitPane) {
                log.debug("{}{} = Item: {}, Parent: {} (Instance: {}, Name: {})", "\t".repeat(level), level, item.getClass().getSimpleName(), parent, item.hashCode(), ((SplitPane) item).getOrientation().name());
            } else if (item instanceof DockNode) {
                DockNode node = (DockNode) item;
                DockTitleBar titleBar = node.getDockTitleBar();
                if (titleBar == null) {
                    log.debug("{}{} = Item: {}, Parent: {} (Instance: {}, Label: {})", "\t".repeat(level), level, item.getClass().getSimpleName(), parent, item.hashCode(), "N/A");
                } else {
                    log.debug("{}{} = Item: {}, Parent: {} (Instance: {}, Label: {})", "\t".repeat(level), level, item.getClass().getSimpleName(), parent, item.hashCode(), titleBar.getLabel().getText());
                }
            } else {
                log.debug("{}{} = Item: {}, Parent: {} (Instance: {})", "\t".repeat(level), level, item.getClass().getSimpleName(), parent, item.hashCode());
            }

            if (item instanceof SplitPane) {
                printDockHierarchy(((SplitPane) item).getItems(), level + 1);
            } else if (item instanceof DockNode) {
                printDockHierarchy(((DockNode) item).getChildren(), level + 1);
            }
        }
    }

    public void setupToggableToolbar(ToolBar toolBar, Node activateOnNode) {
        Pane tbParent = (Pane) toolBar.getParent();

        Runnable removeTbTask = () -> {
            tbParent.getChildren().remove(toolBar);
        };

        EventHandler<KeyEvent> keyEventHandler = event -> {
            if (event.isControlDown() && KeyCode.F.equals(event.getCode())) {
                if (tbParent.getChildren().contains(toolBar)) {
                    if (appService.cancelTask(removeTbTask)) {
                        log.debug("Cancelled scheduled task: {}", removeTbTask);
                    } else {
                        log.debug("Could not cancel task : {}", removeTbTask);
                    }
                    tbParent.getChildren().remove(toolBar);
                } else {
                    tbParent.getChildren().add(toolBar);
                    appService.runTaskAfter(Duration.ofSeconds(5), removeTbTask);
                    log.debug("Scheduled close task : {}", removeTbTask);
                    toolBar.requestFocus();
                }
            } else if (KeyCode.ESCAPE.equals(event.getCode())) {
                if (tbParent.getChildren().contains(toolBar)) {
                    appService.cancelTask(removeTbTask);
                    tbParent.getChildren().remove(toolBar);
                }
            }
        };

        toolBar.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (KeyCode.ESCAPE.equals(event.getCode())) {
                log.debug("Escape pressed");
                if (tbParent.getChildren().contains(toolBar)) {
                    appService.cancelTask(removeTbTask);
                    tbParent.getChildren().remove(toolBar);
                }
            }
            if (tbParent.getChildren().contains(toolBar))
                appService.touchTask(removeTbTask);
        });

        activateOnNode.setOnKeyPressed(keyEventHandler);
        tbParent.getChildren().remove(toolBar);
    }

    @Autowired
    public void setAppService(AppService appService) {
        this.appService = appService;
    }
}
