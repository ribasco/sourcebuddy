package com.ibasco.sourcebuddy.components;

import javafx.scene.Scene;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ThemeManager {

    private static final Logger log = LoggerFactory.getLogger(ThemeManager.class);

    private ConfigurableApplicationContext applicationContext;

    public void applyTheme(String themeUrl) {
        Map<String, Scene> scenes = applicationContext.getBeansOfType(Scene.class);
        log.debug("Scene size: {}", scenes.size());
    }

    @Autowired
    public void setApplicationContext(ConfigurableApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
}
