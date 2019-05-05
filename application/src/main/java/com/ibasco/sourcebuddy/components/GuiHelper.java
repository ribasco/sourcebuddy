package com.ibasco.sourcebuddy.components;

import com.ibasco.sourcebuddy.domain.KeyValueInfo;
import com.ibasco.sourcebuddy.domain.ServerDetails;
import com.ibasco.sourcebuddy.gui.cells.DecoratedTableCell;
import com.ibasco.sourcebuddy.gui.cells.DecoratedTreeTableCell;
import com.ibasco.sourcebuddy.gui.decorators.CellDecorator;
import com.ibasco.sourcebuddy.model.TreeDataModel;
import com.ibasco.sourcebuddy.service.AppService;
import com.ibasco.sourcebuddy.util.Check;
import com.ibasco.sourcebuddy.util.Delta;
import com.ibasco.sourcebuddy.util.WorkProgressCallback;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.collections.transformation.FilteredList;
import javafx.event.EventHandler;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.MasterDetailPane;
import org.controlsfx.control.NotificationPane;
import org.dockfx.DockNode;
import org.dockfx.DockTitleBar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

@Component
public class GuiHelper {

    private static final Logger log = LoggerFactory.getLogger(GuiHelper.class);

    private AppService appService;

    private ViewManager viewManager;

    public static <T> ObjectBinding<ObservableList<T>> createFilteredListBinding(ListProperty<T> listProperty, ObjectProperty<Predicate<T>> predicate) {
        return Bindings.createObjectBinding(() -> {
            if (listProperty.get() != null) {
                FilteredList<T> filteredList = listProperty.filtered(p -> true);
                filteredList.predicateProperty().bind(predicate);
                return filteredList;
            }
            return FXCollections.emptyObservableList();
        }, listProperty);
    }

    public static List<String> extractServerTags(ServerDetails details) {
        Check.requireNonNull(details, "Server details cannot be null");
        String[] tags = StringUtils.split(details.getServerTags(), ";:, ");
        return tags == null ? Collections.emptyList() : Arrays.asList(tags);
    }

    public static <T> void bindContent(List<T> list, ObservableSet<T> set) {
        Check.requireNonNull(list, "List cannot be null");
        Check.requireNonNull(set, "Observable set cannot be null");
        set.addListener((SetChangeListener<T>) change -> {
            if (change.wasAdded()) {
                T element = change.getElementAdded();
                if (element != null)
                    list.add(element);
            } else if (change.wasRemoved()) {
                T element = change.getElementRemoved();
                if (element != null)
                    list.remove(element);
            }
        });
        list.clear();
        list.addAll(set);
    }

    public static TreeItem<ServerDetails> convertToTreeItem(TreeDataModel<ServerDetails> data) {
        TreeItem<ServerDetails> root = new TreeItem<>();
        root.setExpanded(true);
        copyTreeDataToTreeItem(data, root);
        return root;
    }

    public static void copyTreeDataToTreeItem(TreeDataModel<ServerDetails> source, TreeItem<ServerDetails> target) {
        for (TreeDataModel<ServerDetails> child : source.getChildren()) {
            TreeItem<ServerDetails> childRoot = new TreeItem<>(child.getItem());
            childRoot.setExpanded(true);
            target.getChildren().add(childRoot);
            if (!child.getChildren().isEmpty()) {
                copyTreeDataToTreeItem(child, childRoot);
            }
        }
    }

    public static <S, T> TreeDataModel<T> mapToTreeDataModel(Map<S, List<T>> map, Function<S, T> childFactory) {
        TreeDataModel<T> root = new TreeDataModel<>();
        for (var entry : map.entrySet()) {
            List<T> childList = entry.getValue();
            TreeDataModel<T> childRoot = new TreeDataModel<>(childFactory.apply(entry.getKey())); //new ServerDetails(entry.getKey().getName())
            root.getChildren().add(childRoot);
            for (T server : childList) {
                TreeDataModel<T> serverTree = new TreeDataModel<>(server);
                childRoot.getChildren().add(serverTree);
            }
        }
        return root;
    }

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

    public static <S, T> TreeTableColumn<S, T> createBasicTreeColumn(TreeTableView<S> treeTableView, String label, String propertyName) {
        return createBasicTreeColumn(treeTableView, label, propertyName, null, null);
    }

    public static <S, T> TreeTableColumn<S, T> createBasicTreeColumn(TreeTableView<S> treeTableView, String label, String propertyName, Callback<TreeTableColumn<S, T>, TreeTableCell<S, T>> cellFactory) {
        return createBasicTreeColumn(treeTableView, label, propertyName, null, cellFactory);
    }

    public static <S, T> TreeTableColumn<S, T> createBasicTreeColumn(TreeTableView<S> treeTableView, String label, String propertyName, Callback<TreeTableColumn.CellDataFeatures<S, T>, ObservableValue<T>> cellValueFactory, Callback<TreeTableColumn<S, T>, TreeTableCell<S, T>> cellFactory) {
        TreeTableColumn<S, T> column = new TreeTableColumn<>(label);
        if (cellValueFactory != null) {
            column.setCellValueFactory(cellValueFactory);
        } else {
            if (!StringUtils.isBlank(propertyName))
                column.setCellValueFactory(new TreeItemPropertyValueFactory<>(propertyName));
        }
        if (cellFactory != null)
            column.setCellFactory(cellFactory);
        treeTableView.getColumns().add(column);
        return column;
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

    public static <T, U> TreeTableCell<T, U> createTreeTableCell(BiConsumer<U, TreeTableCell<T, U>> callback) {
        return new TreeTableCell<>() {
            @Override
            protected void updateItem(U item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                    setGraphic(null);
                    setStyle(null);
                } else {
                    callback.accept(item, this);
                }
            }
        };
    }

    public static <S, T> TreeTableCell<S, T> createDecoratedTreeTableCell(CellDecorator<S, T> decorator) {
        return new DecoratedTreeTableCell<>(decorator);
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

    public static <T, U> TableCell<T, U> createDecoratedTableCell(CellDecorator<T, U> decorator) {
        return new DecoratedTableCell<>(decorator);
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
        return findNode(node, find, 0, null, null);
    }

    public static <T> T findNode(Parent node, Class<T> find, String id) {
        return findNode(node, find, 0, null, id);
    }

    public static <T> T findNode(Parent node, Class<T> find, Consumer<Node> callback) {
        return findNode(node, find, 0, callback, null);
    }

    public static <T> T findNode(Parent node, Class<T> find, Consumer<Node> callback, String id) {
        return findNode(node, find, 0, callback, id);
    }

    public static <T> T findNode(Parent parent, Class<T> find, int level, Consumer<Node> callback, String id) {
        for (Node child : parent.getChildrenUnmodifiable()) {
            if (callback != null) {
                callback.accept(child);
            }
            if (find != null) {
                if (child.getClass().equals(find)) {
                    if (!StringUtils.isBlank(id) && !id.equalsIgnoreCase(child.getId())) {
                        continue;
                    }
                    //noinspection unchecked
                    return (T) child;
                }
            }
            if (child instanceof Parent) {
                Node found = (Node) findNode((Parent) child, find, level + 1, callback, id);
                if (found != null) {
                    //noinspection unchecked
                    return (T) found;
                }
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
                    appService.runAfter(Duration.ofSeconds(5), removeTbTask);
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
                appService.reset(removeTbTask);
        });

        activateOnNode.setOnKeyPressed(keyEventHandler);
        tbParent.getChildren().remove(toolBar);
    }

    public static NotificationPane findNotificationPane(Parent parent) {
        for (Node node : parent.getChildrenUnmodifiable()) {
            //log.debug("\t> findNotificationPane() :: Processing node: {}", node);
            if (node instanceof NotificationPane)
                return (NotificationPane) node;
            if (node instanceof Parent)
                findNotificationPane((Parent) node);
        }
        return null;
    }

    @Autowired
    public void setAppService(AppService appService) {
        this.appService = appService;
    }

    @Autowired
    public void setViewManager(ViewManager viewManager) {
        this.viewManager = viewManager;
    }
}
