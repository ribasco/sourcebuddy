package com.ibasco.sourcebuddy.util;

import com.ibasco.sourcebuddy.domain.KeyValueInfo;
import javafx.collections.ObservableList;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;
import org.controlsfx.control.MasterDetailPane;
import org.dockfx.DockNode;
import org.dockfx.DockTitleBar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class GuiUtil {

    private static final Logger log = LoggerFactory.getLogger(GuiUtil.class);

    public static void setupKeyValueTable(TableView<KeyValueInfo> tableView) {
        TableColumn<KeyValueInfo, String> nameCol = new TableColumn<>("Property");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("key"));

        TableColumn<KeyValueInfo, String> valueCol = new TableColumn<>("Value");
        valueCol.setCellValueFactory(new PropertyValueFactory<>("value"));

        //noinspection unchecked
        tableView.getColumns().addAll(nameCol, valueCol);
    }

    public static <A, B> TableColumn<A, B> createBasicColumn(TableView<A> table, String label, String propertyName) {
        return createBasicColumn(table, label, propertyName, null);
    }

    public static <A, B> TableColumn<A, B> createBasicColumn(TableView<A> table, String label, String propertyName, Callback<TableColumn<A, B>, TableCell<A, B>> cellFactory) {
        TableColumn<A, B> column = new TableColumn<>(label);
        if (propertyName != null && !propertyName.isBlank())
            column.setCellValueFactory(new PropertyValueFactory<>(propertyName));
        if (cellFactory != null) {
            column.setCellFactory(cellFactory);
        }
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
}
