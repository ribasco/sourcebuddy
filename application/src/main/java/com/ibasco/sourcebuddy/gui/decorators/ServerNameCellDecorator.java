package com.ibasco.sourcebuddy.gui.decorators;

import com.ibasco.sourcebuddy.domain.ServerDetails;
import javafx.scene.control.IndexedCell;
import org.springframework.stereotype.Component;

@Component
public class ServerNameCellDecorator implements CellDecorator<ServerDetails, String> {

    @Override
    public void decorate(String item, IndexedCell<ServerDetails> cell) {
        cell.getStyleClass().clear();
        cell.getStyleClass().add("server-name-col");
        cell.setText(item);
    }
}
