package com.ibasco.sourcebuddy.util.factory;

import com.ibasco.sourcebuddy.components.GuiHelper;
import com.ibasco.sourcebuddy.domain.Country;
import com.ibasco.sourcebuddy.domain.ServerDetails;
import com.ibasco.sourcebuddy.domain.SteamApp;
import com.ibasco.sourcebuddy.enums.ServerStatus;
import com.ibasco.sourcebuddy.service.SourceServerService;
import com.ibasco.sourcebuddy.util.ResourceUtil;
import javafx.beans.property.BooleanProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import org.apache.commons.lang3.RandomUtils;
import org.controlsfx.control.Rating;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

@Component
public class ServerBrowserTableViewFactory {

    private static final Logger log = LoggerFactory.getLogger(ServerBrowserTableViewFactory.class);

    private Map<String, Image> iconCache = new HashMap<>();

    private SourceServerService sourceServerQueryService;

    @Autowired
    public ServerBrowserTableViewFactory(SourceServerService sourceServerQueryService) {
        this.sourceServerQueryService = sourceServerQueryService;
    }

    public TableCell<ServerDetails, Country> drawCountryIcon(TableColumn<ServerDetails, Country> column) {
        return GuiHelper.createTableCell((country, cell) -> {
            cell.setGraphic(createIconFromCountryCode(country.getCountryCode()));
            cell.setText(country.getCountryName());
            cell.setAlignment(Pos.CENTER_LEFT);
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
        return GuiHelper.createTableCell((item, cell) -> {
            cell.getStyleClass().clear();
            cell.getStyleClass().add("server-ip-address");
            cell.setText(item.getAddress().getHostAddress() + ":" + item.getPort());
        });
    }

    public TableCell<ServerDetails, SteamApp> drawSteamApp(TableColumn<ServerDetails, SteamApp> abTableColumn) {
        return GuiHelper.createTableCell((item, cell) -> {
            cell.setText(item.getName());
            //cell.setGraphic(new Hyperlink(item.toString()));
            cell.setAlignment(Pos.CENTER);

        });
    }

    public TableCell<ServerDetails, ServerStatus> drawStatusInd(TableColumn<ServerDetails, ServerStatus> column) {
        return GuiHelper.createTableCell((serverStatus, cell) -> {
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
        });
    }

    public TableCell<ServerDetails, Boolean> drawBookmarkNode(TableColumn<ServerDetails, Boolean> column) {
        return GuiHelper.createTableCell((item, cell) -> {
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

    public TableCell<ServerDetails, String> drawTags(TableColumn<ServerDetails, String> column) {
        return GuiHelper.createTableCell((item, cell) -> {
            HBox hbox = new HBox();
            hbox.setSpacing(5);
            String[] tags = item.split(",");
            for (String tag : tags) {
                Label lTag = new Label(tag);
                //lTag.setStyle("-fx-background-color: rgba(117,157,255,0.84); -fx-text-fill: black; -fx-border-radius: 5 5 5 5; -fx-background-radius: 5 5 5 5; -fx-padding: 5px; -fx-font-weight: bold;");

                Color background = randColor();
                Color inverted = background.invert();
                String backgroundStr = String.format("rgba(%d,%d,%d,%f)", conv(background.getRed()), conv(background.getGreen()), conv(background.getBlue()), background.getOpacity());
                String textFill = String.format("rgba(%d,%d,%d,%f)", conv(inverted.getRed()), conv(inverted.getGreen()), conv(inverted.getBlue()), inverted.getOpacity());

                lTag.setStyle(String.format("-fx-background-color: %s; -fx-text-fill: %s; -fx-border-radius: 5 5 5 5; -fx-background-radius: 5 5 5 5; -fx-padding: 5px; -fx-font-weight: bold;", backgroundStr, "black"));

                hbox.getChildren().add(lTag);
            }
            cell.setGraphic(hbox);
        });
    }

    private int conv(double val) {
        return (int) map(val, 0.0, 0.1, 0, 255);
    }

    private double map(double x, double in_min, double in_max, double out_min, double out_max) {
        return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
    }

    private Color randColor() {
        int r = RandomUtils.nextInt(0, 255);
        int g = RandomUtils.nextInt(0, 255);
        int b = RandomUtils.nextInt(0, 255);
        double o = RandomUtils.nextDouble(0.6f, 1.0f);
        return Color.rgb(r, g, b, o);//String.format("rgba(%d,%d,%d,%f)", r, g, b, o);
    }

    public TableCell<ServerDetails, String> drawServerName(TableColumn<ServerDetails, String> column) {
        return GuiHelper.createTableCell((item, cell) -> {
            //cell.setTextFill(Color.PINK); //Color.web("#E8BC20")
            /*cell.setStyle("-fx-font-weight: bold; -fx-text-fill: #E8BC20");
            cell.setAlignment(Pos.CENTER_LEFT);*/
            cell.getStyleClass().clear();
            cell.getStyleClass().add("server-name-col");
            cell.setText(item);
        });
    }
}
