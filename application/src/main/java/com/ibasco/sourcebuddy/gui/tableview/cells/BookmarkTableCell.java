package com.ibasco.sourcebuddy.gui.tableview.cells;

import javafx.beans.property.BooleanProperty;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import javafx.scene.input.MouseEvent;
import org.controlsfx.control.Rating;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.BiConsumer;

public class BookmarkTableCell<T> extends TableCell<T, Boolean> {

    private static final Logger log = LoggerFactory.getLogger(BookmarkTableCell.class);

    private Rating rating;

    public BookmarkTableCell(BiConsumer<T, BooleanProperty> callback) {
        this.rating = new Rating(1);

        EventHandler<MouseEvent> clickHandler = new EventHandler<>() {
            private BookmarkTableCell<T> cell = BookmarkTableCell.this;

            @Override
            public void handle(MouseEvent event) {
                BooleanProperty property = (BooleanProperty) cell.getTableColumn().getCellObservableValue(cell.getIndex());
                property.setValue(!property.getValue());
                T item = cell.getTableRow().getItem();
                callback.accept(item, property);
                event.consume();
            }
        };

        this.rating.setOnMouseClicked(clickHandler);
        rating.getStyleClass().clear();
        rating.getStyleClass().add("bookmark");
    }

    @Override
    protected void updateItem(Boolean item, boolean empty) {
        super.updateItem(item, empty);
        if (item == null || empty) {
            setText(null);
            setStyle("");
        } else {
            rating.setRating(item ? 1 : 0);
            this.setGraphic(rating);
            this.setAlignment(Pos.CENTER);
        }
    }
}
