package com.ibasco.sourcebuddy.gui.decorators;

import com.ibasco.sourcebuddy.domain.ServerDetails;
import com.ibasco.sourcebuddy.enums.ServerStatus;
import javafx.geometry.Pos;
import javafx.scene.control.IndexedCell;
import javafx.scene.text.TextAlignment;
import org.springframework.stereotype.Component;

@Component
public class ServerStatusCellDecorator implements CellDecorator<ServerDetails, ServerStatus> {

    @Override
    public void decorate(ServerStatus serverStatus, IndexedCell<ServerDetails> cell) {
        switch (serverStatus) {
            case ACTIVE:
                cell.getStyleClass().setAll("server-status-active");
                break;
            case INACTIVE:
                cell.getStyleClass().setAll("server-status-inactive");
                break;
            case TIMED_OUT:
                cell.getStyleClass().setAll("server-status-timedout");
                break;
        }
        cell.setText(serverStatus.getDescription().toUpperCase());
        cell.setAlignment(Pos.CENTER);
        cell.setTextAlignment(TextAlignment.CENTER);
    }
}
