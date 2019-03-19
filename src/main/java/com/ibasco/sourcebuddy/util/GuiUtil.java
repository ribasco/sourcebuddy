package com.ibasco.sourcebuddy.util;

import com.ibasco.sourcebuddy.entities.KeyValueInfo;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class GuiUtil {
    public static void setupKeyValueTable(TableView<KeyValueInfo> tableView) {
        TableColumn<KeyValueInfo, String> nameCol = new TableColumn<>("Property");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("key"));

        TableColumn<KeyValueInfo, String> valueCol = new TableColumn<>("Value");
        valueCol.setCellValueFactory(new PropertyValueFactory<>("value"));

        //noinspection unchecked
        tableView.getColumns().addAll(nameCol, valueCol);
    }
}
