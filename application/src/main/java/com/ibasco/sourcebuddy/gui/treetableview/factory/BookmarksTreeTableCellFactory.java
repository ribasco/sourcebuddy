package com.ibasco.sourcebuddy.gui.treetableview.factory;

import static com.ibasco.sourcebuddy.components.GuiHelper.createDecoratedTreeTableCell;
import com.ibasco.sourcebuddy.domain.Country;
import com.ibasco.sourcebuddy.domain.ServerDetails;
import com.ibasco.sourcebuddy.enums.OperatingSystem;
import com.ibasco.sourcebuddy.enums.ServerStatus;
import com.ibasco.sourcebuddy.gui.decorators.ServerCountryCellDecorator;
import com.ibasco.sourcebuddy.gui.decorators.ServerNameCellDecorator;
import com.ibasco.sourcebuddy.gui.decorators.ServerStatusCellDecorator;
import com.ibasco.sourcebuddy.gui.decorators.ServerTagCellDecorator;
import com.ibasco.sourcebuddy.gui.treetableview.cells.BookmarksTreeTableCell;
import com.ibasco.sourcebuddy.gui.treetableview.cells.FormattedTreeTableCell;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.util.Callback;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BookmarksTreeTableCellFactory implements Callback<TreeTableColumn<ServerDetails, Object>, TreeTableCell<ServerDetails, Object>> {

    private ServerCountryCellDecorator countryCellDecorator;

    private ServerStatusCellDecorator serverStatusCellDecorator;

    private ServerTagCellDecorator serverTagCellDecorator;

    private ServerNameCellDecorator serverNameCellDecorator;

    @SuppressWarnings("Duplicates")
    public TreeTableCell<ServerDetails, ServerStatus> statusIndicator(TreeTableColumn<ServerDetails, ServerStatus> col) {
        return new BookmarksTreeTableCell<>(serverStatusCellDecorator);
    }

    public TreeTableCell<ServerDetails, Country> country(TreeTableColumn<ServerDetails, Country> column) {
        return new BookmarksTreeTableCell<>(countryCellDecorator);
    }

    public TreeTableCell<ServerDetails, String> serverTags(TreeTableColumn<ServerDetails, String> column) {
        return new BookmarksTreeTableCell<>(serverTagCellDecorator);
    }

    public TreeTableCell<ServerDetails, String> serverName(TreeTableColumn<ServerDetails, String> column) {
        return new FormattedTreeTableCell<>("root-col", p -> StringUtils.isBlank(p.getIpAddress()));//new BookmarksTreeTableCell<>(serverNameCellDecorator);
    }

    public TreeTableCell<ServerDetails, OperatingSystem> operatingSystem(TreeTableColumn<ServerDetails, OperatingSystem> col) {
        return createDecoratedTreeTableCell((item, cell) -> cell.setText(WordUtils.capitalizeFully(item.name())));
    }

    @Override
    public TreeTableCell<ServerDetails, Object> call(TreeTableColumn<ServerDetails, Object> param) {
        return new BookmarksTreeTableCell<>();
    }

    @Autowired
    public void setCountryCellDecorator(ServerCountryCellDecorator countryCellDecorator) {
        this.countryCellDecorator = countryCellDecorator;
    }

    @Autowired
    public void setServerStatusCellDecorator(ServerStatusCellDecorator serverStatusCellDecorator) {
        this.serverStatusCellDecorator = serverStatusCellDecorator;
    }

    @Autowired
    public void setServerTagCellDecorator(ServerTagCellDecorator serverTagCellDecorator) {
        this.serverTagCellDecorator = serverTagCellDecorator;
    }
}
