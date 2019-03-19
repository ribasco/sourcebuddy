package com.ibasco.sourcebuddy.util;

import com.ibasco.sourcebuddy.Bootstrap;
import com.ibasco.sourcebuddy.exceptions.ResourceLoadException;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.net.URL;

public class ResourceUtil {
    public static URL loadResource(String resourceName) {
        if (resourceName == null || resourceName.isBlank())
            throw new IllegalArgumentException("Resource name cannot be null or blank");
        URL viewUrl = Bootstrap.class.getResource(resourceName);
        if (viewUrl == null)
            throw new ResourceLoadException("Resource not found: " + resourceName);
        return viewUrl;
    }

    public static ImageView loadIconView(String name) {
        ImageView image = new ImageView(loadIcon(name));
        image.setFitWidth(16);
        image.setFitHeight(16);
        return image;
    }

    public static Image loadIcon(String name) {
        String resPath;
        if (name.startsWith("/")) {
            resPath = String.format("/icons%s.png", name);
        } else {
            resPath = String.format("/icons/%s.png", name);
        }
        return new Image(loadResource(resPath).toExternalForm());
    }
}
