package com.ibasco.sourcebuddy.components;

import com.ibasco.sourcebuddy.util.Check;
import com.ibasco.sourcebuddy.util.ResourceUtil;
import javafx.application.Platform;
import javafx.scene.image.ImageView;
import org.controlsfx.control.NotificationPane;
import org.controlsfx.control.action.Action;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * Note: There can only be one notification pane for each stage
 */
@Component
@Scope("prototype")
public class NotificationManager {

    private NotificationPane pane;

    private ImageView errorIcon;

    private ImageView infoIcon;

    private ImageView warningIcon;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public NotificationManager(NotificationPane pane) {
        this.pane = Check.requireNonNull(pane, "Notification pane is null");
    }

    @PostConstruct
    private void postInit() {
        this.infoIcon = ResourceUtil.loadIconView("/notif/notif-icon-info");
        this.warningIcon = ResourceUtil.loadIconView("/notif/notif-icon-warning-round");
        this.errorIcon = ResourceUtil.loadIconView("/notif/notif-icon-error-round");
    }

    public void showInfo(String message, Object... args) {
        showNotification(String.format(message, args), infoIcon, "info", null);
    }

    public void showInfo(String message, List<Action> actions) {
        showNotification(message, infoIcon, "info", actions);
    }

    public void showWarning(String message, Object... args) {
        showNotification(String.format(message, args), warningIcon, "warning", null);
    }

    public void showWarning(String message, List<Action> actions) {
        showNotification(message, warningIcon, "warning", actions);
    }

    public void showError(String message, Object... args) {
        showNotification(String.format(message, args), errorIcon, "error", null);
    }

    public void showError(String message, List<Action> actions) {
        showNotification(message, errorIcon, "error", actions);
    }

    private void showNotification(String message, ImageView graphic, String style, List<Action> actions) {
        Runnable action = () -> {
            if (!pane.getStyleClass().contains(style))
                pane.getStyleClass().add(style);
            if (pane.isShowing())
                pane.hide();
            pane.setGraphic(graphic);
            if (actions != null && !actions.isEmpty()) {
                pane.getActions().clear();
                pane.getActions().addAll(actions);
            }
            pane.setText(message);
            pane.show();
        };

        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(action);
            return;
        }
        action.run();
    }
}
