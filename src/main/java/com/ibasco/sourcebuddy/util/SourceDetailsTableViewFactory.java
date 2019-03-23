package com.ibasco.sourcebuddy.util;

import com.ibasco.sourcebuddy.domain.Country;
import com.ibasco.sourcebuddy.domain.ServerDetails;
import com.ibasco.sourcebuddy.domain.SteamApp;
import com.ibasco.sourcebuddy.enums.ServerStatus;
import static com.ibasco.sourcebuddy.util.GuiUtil.createTableCell;
import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.TextAlignment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

@Component
public class SourceDetailsTableViewFactory {

    private static final Logger log = LoggerFactory.getLogger(SourceDetailsTableViewFactory.class);

    private Map<String, Image> iconCache = new HashMap<>();

    public TableCell<ServerDetails, Country> drawCountryIcon(TableColumn<ServerDetails, Country> column) {
        return createTableCell((country, cell) -> {
            cell.setGraphic(createIconFromCountryCode(country.getCountryCode()));
            cell.setText(country.getCountryName());
            cell.setAlignment(Pos.CENTER);
            cell.setTextAlignment(TextAlignment.RIGHT);
        });
    }

    private ImageView createIconFromCountryCode(String countryCode) {
        String key = countryCode.toLowerCase();
        Image iconImage = iconCache.computeIfAbsent(key, s -> ResourceUtil.loadIcon(String.format("/country/%s", key)));
        ImageView icon = new ImageView(iconImage);
        icon.setPreserveRatio(true);
        icon.setFitWidth(16);
        return icon;
    }

    public TableCell<ServerDetails, InetSocketAddress> drawIpAddress(TableColumn<ServerDetails, InetSocketAddress> column) {
        return createTableCell((item, cell) -> cell.setText(item.getAddress().getHostAddress() + ":" + item.getPort()));
    }

    public TableCell<ServerDetails, SteamApp> drawSteamApp(TableColumn<ServerDetails, SteamApp> abTableColumn) {
        return createTableCell((item, cell) -> {

            cell.setText(item.getName());
            //cell.setGraphic(new Hyperlink(item.toString()));
            cell.setAlignment(Pos.CENTER);

        });
    }

    public TableCell<ServerDetails, ServerStatus> drawStatusInd(TableColumn<ServerDetails, ServerStatus> column) {
        return createTableCell((serverStatus, cell) -> {
            switch (serverStatus) {
                case ACTIVE:
                    cell.setStyle("-fx-font-weight: bolder; -fx-text-fill: green");
                    break;
                case INACTIVE:
                    cell.setStyle("-fx-font-weight: bold; -fx-text-fill: darkgray");
                    break;
                case TIMED_OUT:
                    cell.setStyle("-fx-font-weight: bolder; -fx-text-fill: red");
                    break;
            }
            cell.setText(serverStatus.getDescription().toUpperCase());
            cell.setAlignment(Pos.CENTER);
            cell.setTextAlignment(TextAlignment.CENTER);
        });
    }
}
