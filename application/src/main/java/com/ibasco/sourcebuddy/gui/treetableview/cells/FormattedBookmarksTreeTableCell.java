package com.ibasco.sourcebuddy.gui.treetableview.cells;

import com.ibasco.sourcebuddy.domain.ServerDetails;
import com.ibasco.sourcebuddy.gui.decorators.CellDecorator;

public class FormattedBookmarksTreeTableCell<T> extends ServerDetailsTreeTableCell<T> {

    public FormattedBookmarksTreeTableCell(CellDecorator<ServerDetails, T> decorator) {
        super(decorator);
    }
}
