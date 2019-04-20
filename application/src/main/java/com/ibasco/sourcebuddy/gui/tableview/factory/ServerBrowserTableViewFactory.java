package com.ibasco.sourcebuddy.gui.tableview.factory;

import static com.ibasco.sourcebuddy.components.GuiHelper.createDecoratedTableCell;
import static com.ibasco.sourcebuddy.components.GuiHelper.createTableCell;
import com.ibasco.sourcebuddy.domain.Country;
import com.ibasco.sourcebuddy.domain.ServerDetails;
import com.ibasco.sourcebuddy.domain.SteamApp;
import com.ibasco.sourcebuddy.enums.OperatingSystem;
import com.ibasco.sourcebuddy.enums.ServerStatus;
import com.ibasco.sourcebuddy.gui.decorators.ServerCountryCellDecorator;
import com.ibasco.sourcebuddy.gui.decorators.ServerNameCellDecorator;
import com.ibasco.sourcebuddy.gui.decorators.ServerStatusCellDecorator;
import com.ibasco.sourcebuddy.gui.decorators.ServerTagCellDecorator;
import com.ibasco.sourcebuddy.service.SourceServerService;
import javafx.beans.property.BooleanProperty;
import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.input.MouseEvent;
import org.apache.commons.text.WordUtils;
import org.controlsfx.control.Rating;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.function.BiConsumer;

@Component
public class ServerBrowserTableViewFactory {

    private static final Logger log = LoggerFactory.getLogger(ServerBrowserTableViewFactory.class);

    private SourceServerService sourceServerQueryService;

    private ServerCountryCellDecorator serverCountryCellDecorator;

    private ServerTagCellDecorator serverTagCellDecorator;

    private ServerStatusCellDecorator serverStatusCellDecorator;

    private ServerNameCellDecorator serverNameCellDecorator;

    public TableCell<ServerDetails, Country> country(TableColumn<ServerDetails, Country> column) {
        return createDecoratedTableCell(serverCountryCellDecorator);
    }

    public TableCell<ServerDetails, InetSocketAddress> drawIpAddress(TableColumn<ServerDetails, InetSocketAddress> column) {
        return createTableCell((item, cell) -> {
            cell.getStyleClass().clear();
            cell.getStyleClass().add("server-ip-address");
            cell.setText(item.getAddress().getHostAddress() + ":" + item.getPort());
        });
    }

    public TableCell<ServerDetails, SteamApp> steamApp(TableColumn<ServerDetails, SteamApp> abTableColumn) {
        return createTableCell((item, cell) -> {
            cell.setText(item.getName());
            //cell.setGraphic(new Hyperlink(item.toString()));
            cell.setAlignment(Pos.CENTER);

        });
    }

    public TableCell<ServerDetails, ServerStatus> statusInd(TableColumn<ServerDetails, ServerStatus> column) {
        return createDecoratedTableCell(serverStatusCellDecorator);
    }

    public TableCell<ServerDetails, Boolean> bookmark(TableColumn<ServerDetails, Boolean> column) {
        return createTableCell((item, cell) -> {
            Rating rating = new Rating(1);
            BooleanProperty bp = (BooleanProperty) cell.getTableColumn().getCellObservableValue(cell.getIndex());
            rating.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
                bp.setValue(!bp.getValue());
                ServerDetails server = cell.getTableRow().getItem();
                sourceServerQueryService.updateBookmarkFlag(server, bp.getValue());
                event.consume();
            });
            rating.setRating(item ? 1 : 0);
            cell.setGraphic(rating);
            cell.setAlignment(Pos.CENTER);
        });
    }

    public TableCell<ServerDetails, String> tags(TableColumn<ServerDetails, String> column) {
        return createDecoratedTableCell(serverTagCellDecorator);
    }

    public TableCell<ServerDetails, String> serverName(TableColumn<ServerDetails, String> column) {
        return createDecoratedTableCell(serverNameCellDecorator);
    }

    public TableCell<ServerDetails, OperatingSystem> operatingSystem(TableColumn<ServerDetails, OperatingSystem> column) {
        return createTableCell(new BiConsumer<OperatingSystem, TableCell<ServerDetails, OperatingSystem>>() {
            @Override
            public void accept(OperatingSystem os, TableCell<ServerDetails, OperatingSystem> cell) {
                cell.setText(WordUtils.capitalizeFully(os.toString()));
            }
        });
    }

    @Autowired
    public ServerBrowserTableViewFactory(SourceServerService sourceServerQueryService) {
        this.sourceServerQueryService = sourceServerQueryService;
    }

    @Autowired
    public void setServerCountryCellDecorator(ServerCountryCellDecorator serverCountryCellDecorator) {
        this.serverCountryCellDecorator = serverCountryCellDecorator;
    }

    @Autowired
    public void setServerTagCellDecorator(ServerTagCellDecorator serverTagCellDecorator) {
        this.serverTagCellDecorator = serverTagCellDecorator;
    }

    @Autowired
    public void setServerStatusCellDecorator(ServerStatusCellDecorator serverStatusCellDecorator) {
        this.serverStatusCellDecorator = serverStatusCellDecorator;
    }

    @Autowired
    public void setServerNameCellDecorator(ServerNameCellDecorator serverNameCellDecorator) {
        this.serverNameCellDecorator = serverNameCellDecorator;
    }
}
