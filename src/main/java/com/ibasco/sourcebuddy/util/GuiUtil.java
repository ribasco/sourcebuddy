package com.ibasco.sourcebuddy.util;

import com.ibasco.sourcebuddy.domain.KeyValueInfo;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

import java.util.List;
import java.util.function.BiConsumer;

public class GuiUtil {

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
            if (!target.contains(svr)) {
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
}
