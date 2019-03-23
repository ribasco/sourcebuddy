package com.ibasco.sourcebuddy.components;

import com.ibasco.sourcebuddy.Bootstrap;
import javafx.scene.image.ImageView;
import org.controlsfx.control.NotificationPane;
import org.controlsfx.control.action.Action;
import org.springframework.stereotype.Component;

import java.net.URL;

@Component
public class NotificationManager {

    public void showWarning(NotificationPane pane, String message, Action... actions) {
        showNotification(pane, message, "/icons/notif/notif-icon-warning.png", actions);
    }

    public void showNotification(NotificationPane pane, String message, String graphic, Action... actions) {
        if (pane.isShowing())
            pane.hide();
        if (graphic != null && !graphic.isBlank()) {
            URL imagePath = Bootstrap.class.getResource(graphic);
            if (imagePath == null) {
                throw new NullPointerException("Image path not found: " + graphic);
            }
            ImageView image = new ImageView(imagePath.toExternalForm());
            pane.setGraphic(image);
        }

        if (actions != null) {
            pane.getActions().clear();
            pane.getActions().addAll(actions);
        }
        pane.setText(message);
        pane.show();
    }

    public void showError(NotificationPane pane, String message, Action... actions) {
        showNotification(pane, message, "/icons/notif/notif-icon-error.png", actions);
    }
}
