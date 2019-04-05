package com.ibasco.sourcebuddy.gui.tableview.cells;

import com.ibasco.agql.core.exceptions.TooManyRequestsException;
import com.ibasco.sourcebuddy.domain.SteamApp;
import com.ibasco.sourcebuddy.domain.SteamAppDetails;
import com.ibasco.sourcebuddy.service.SteamService;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableCell;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
@Scope("prototype")
public class SteamAppTableCell extends TableCell<SteamApp, Integer> {

    private static final Logger log = LoggerFactory.getLogger(SteamAppTableCell.class);

    private static final Map<SteamApp, AppDetailCacheEntry> steamCache = new ConcurrentHashMap<>();

    private static AtomicLong timeout = new AtomicLong(-1);

    private static final int TIMEOUT_INTERVAL = 300;

    private SteamService steamService;

    private ProgressIndicator pi = new ProgressIndicator();

    private class AppDetailCacheEntry {

        Image image;

        SteamAppDetails details;

        private AppDetailCacheEntry(Image image, SteamAppDetails details) {
            this.image = image;
            this.details = details;
        }
    }

    public SteamAppTableCell() {
        pi.setPrefSize(16, 16);
    }

    @Override
    protected void updateItem(Integer appId, boolean empty) {
        super.updateItem(appId, empty);
        if (appId == null || empty) {
            setText(null);
            setStyle("");
        } else {
            if (getTableRow() == null)
                return;
            SteamApp app = getTableRow().getItem();

            if (app == null) {
                log.warn("App instance is null for id {}", appId);
                setGraphic(null);
                setText(null);
                return;
            }

            setAlignment(Pos.CENTER_LEFT);
            setTextFill(Color.BLACK);
            setTextAlignment(TextAlignment.LEFT);
            setGraphic(pi);

            AppDetailCacheEntry cacheEntry = steamCache.get(app);

            //Check existing cached entry
            if (cacheEntry != null) {
                log.debug("findAppDetails({}) :: Got details from cache", app.getId());
                SteamAppDetails details = cacheEntry.details;
                updateCell(details, cacheEntry.image);
                return;
            }

            //Check timeout
            if (timeout.get() > 0) {
                //Check interval
                long interval = Duration.ofMillis(System.currentTimeMillis() - timeout.get()).toSeconds();
                if (interval < TIMEOUT_INTERVAL) {
                    log.warn("findAppDetails({}) :: Cannot issue query while timeout is in-effect (Will issue again after {} seconds)", app.getId(), TIMEOUT_INTERVAL - interval);
                    setGraphic(null);
                    setTextFill(Color.RED);
                    setText(app.getName());
                    return;
                } else {
                    log.debug("findAppDetails({}) :: Timeout expired. Proceeding with query", app.getId());
                    timeout.set(-1);
                }
            }

            //Find details
            steamService.findAppDetails(app, true).whenComplete((steamAppDetails, ex) -> {
                //Check for errors
                if (ex != null) {
                    try {
                        if (ex instanceof CompletionException && (ex.getCause() instanceof TooManyRequestsException)) {
                            if (timeout.compareAndSet(-1, System.currentTimeMillis())) {
                                log.error("Too many requests sent. Could not retrieve details for {}. Setting timeout", app.getId());
                            }
                            return;
                        }
                        log.error(String.format("findAppDetails(%d) :: Unexpected exception during query %s", app.getId(), ex.getClass().getSimpleName()), ex.getCause());
                    } finally {
                        Platform.runLater(() -> setGraphic(null));
                    }
                    return;
                }

                //Details existing in repository
                log.debug("findAppDetails({}) :: Got details: {} (Cache Size: {})", app.getId(), steamAppDetails, steamCache.size());

                Image img = steamAppDetails.getHeaderImage() != null ? new Image(new ByteArrayInputStream(steamAppDetails.getHeaderImage())) : null;
                steamCache.putIfAbsent(app, new AppDetailCacheEntry(img, steamAppDetails));
                updateCell(steamAppDetails, img);
            });
        }
    }

    private void updateCell(SteamAppDetails appDetails, Image img) {
        Runnable action = () -> {
            setTextAlignment(TextAlignment.CENTER);
            setAlignment(Pos.BOTTOM_RIGHT);

            if (appDetails.getHeaderImage() != null) {
                Tooltip tooltip = new Tooltip();
                if (!StringUtils.isBlank(appDetails.getShortDescription())) {
                    tooltip.setText(String.format("%s\n\n%s", appDetails.getName(), appDetails.getShortDescription()));
                } else {
                    tooltip.setText(appDetails.getName());
                }
                tooltip.setMinWidth(200);
                tooltip.setMaxWidth(300);
                tooltip.setWrapText(true);
                tooltip.setShowDelay(javafx.util.Duration.millis(50));
                setTooltip(tooltip);
                setGraphic(createImageView(img));
                setText(null);
            } else {
                setGraphic(null);
                if (appDetails.isEmptyDetails()) {
                    setTextFill(Color.LIGHTGRAY);
                } else {
                    setTextFill(Color.BLACK);
                }
                setText(appDetails.getName());
            }
        };
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(action);
            return;
        }
        action.run();
    }

    private ImageView createImageView(Image img) {
        if (img == null)
            return null;
        ImageView iv = new ImageView(img);
        iv.setPreserveRatio(true);
        iv.setFitWidth(230);
        return iv;
    }

    @Autowired
    public void setSteamService(SteamService steamService) {
        this.steamService = steamService;
    }
}
