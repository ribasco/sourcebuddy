package com.ibasco.sourcebuddy.gui.treetableview.cells;

import com.ibasco.sourcebuddy.domain.ServerDetails;
import com.ibasco.sourcebuddy.gui.cells.DecoratedTreeTableCell;
import com.ibasco.sourcebuddy.gui.decorators.CellDecorator;
import org.apache.commons.lang3.StringUtils;

public class ServerDetailsTreeTableCell<T> extends DecoratedTreeTableCell<ServerDetails, T> {

    public ServerDetailsTreeTableCell() {
        this(null);
    }

    public ServerDetailsTreeTableCell(CellDecorator<ServerDetails, T> decorator) {
        super(decorator);
    }

    @Override
    protected final void updateItem(T item, boolean empty) {
        ServerDetails details = getTreeTableRow().getItem();
        if (details != null) {
            //Dont display if we are dealing with a root node
            if (StringUtils.isBlank(details.getIpAddress())) {
                setText(null);
                setGraphic(null);
                return;
            }
        }
        super.updateItem(item, empty);
    }
}
