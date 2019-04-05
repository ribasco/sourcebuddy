package com.ibasco.sourcebuddy.gui.tableview.rows;

import javafx.collections.ObservableList;
import javafx.scene.control.TableRow;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.function.Predicate;

public class HighlightRow<T> extends TableRow<T> {

    private static final Logger log = LoggerFactory.getLogger(HighlightRow.class);

    private Predicate<T> predicate;

    private String highlightClass;

    public HighlightRow(Predicate<T> predicate, String highlightClass) {
        this.predicate = predicate;
        this.highlightClass = highlightClass;
    }

    @Override
    protected void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);
        if (item == null || empty) {
            setText(null);
            setGraphic(null);
            setStyle(null);
        } else {
            ObservableList<String> styleClass = getStyleClass();
            if (predicate.test(item)) {
                if (!styleClass.contains(highlightClass))
                    styleClass.add(highlightClass);
            } else {
                styleClass.removeAll(Collections.singleton(highlightClass));
            }
            log.info("{} = {}", item, StringUtils.join(getStyleClass(), ","));

        }
    }
}
