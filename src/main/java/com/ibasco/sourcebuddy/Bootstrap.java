package com.ibasco.sourcebuddy;

import com.ibasco.sourcebuddy.components.ViewManager;
import com.ibasco.sourcebuddy.config.AppConfig;
import com.ibasco.sourcebuddy.constants.Beans;
import com.ibasco.sourcebuddy.constants.Icons;
import com.ibasco.sourcebuddy.constants.Views;
import com.ibasco.sourcebuddy.util.ResourceUtil;
import com.ibasco.sourcebuddy.util.SpringUtil;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.dialog.ExceptionDialog;
import org.dockfx.DockPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.net.URL;

@SpringBootApplication
public class Bootstrap extends Application {

    private static final Logger log = LoggerFactory.getLogger(Bootstrap.class);

    private ConfigurableApplicationContext context;

    private ViewManager viewManager;

    @Value("${app.title}")
    private String appTitle;

    @Override
    public void init() throws Exception {
        log.debug("Bootstrap :: init()");
        SpringApplicationBuilder builder = new SpringApplicationBuilder(Bootstrap.class).sources(AppConfig.class);
        context = builder.run(getParameters().getRaw().toArray(new String[0]));
        SpringUtil.registerBean(Beans.APP_PARAMETERS, getParameters());
        SpringUtil.registerBean(Beans.HOST_SERVICES, getHostServices());
        viewManager = SpringUtil.getBean(Beans.VIEW_MANAGER);
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            log.error("Uncaught exception occured", e);
            Platform.runLater(() -> {
                ExceptionDialog exceptionDialog = new ExceptionDialog(e);
                exceptionDialog.show();
            });
        });
    }

    @Override
    public void start(Stage stage) throws Exception {
        log.debug("Bootstrap :: start()");

        Font appFont = Font.loadFont(ResourceUtil.loadResourceAsStream("/fonts/consola.ttf"), 12);
        Parent rootNode = viewManager.loadView(Views.MAIN);
        SpringUtil.registerBean(Beans.PRIMARY_STAGE, stage);
        SpringUtil.registerSingleton("appFont", appFont);

        log.debug("Bootstrap :: Initializing primary stage and scene");
        Scene scene = new Scene(rootNode);

        URL res = ResourceUtil.loadResource("/styles/default.css");
        scene.getStylesheets().add(res.toExternalForm());

        stage.getIcons().add(ResourceUtil.loadIcon(Icons.APP_ICON));
        stage.setTitle(StringUtils.defaultString(appTitle, "Source Buddy"));
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.setWidth(1280);
        stage.setHeight(720);
        stage.show();

        log.debug("Bootstrap :: Primary stage and scene initialized");

        // test the look and feel with both Caspian and Modena
        Application.setUserAgentStylesheet(Application.STYLESHEET_MODENA);
        DockPane.initializeDefaultUserAgentStylesheet();
    }

    @Override
    public void stop() throws Exception {
        log.info("Stopping application");
        context.close();
    }
}
