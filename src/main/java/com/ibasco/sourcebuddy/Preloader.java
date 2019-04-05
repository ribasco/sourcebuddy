package com.ibasco.sourcebuddy;

import com.ibasco.sourcebuddy.components.GuiHelper;
import com.ibasco.sourcebuddy.components.ViewManager;
import com.ibasco.sourcebuddy.constants.Icons;
import com.ibasco.sourcebuddy.model.PreloadModel;
import com.ibasco.sourcebuddy.util.InfoNotification;
import com.ibasco.sourcebuddy.util.ResourceUtil;
import com.ibasco.sourcebuddy.util.SourceBuddyBanner;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.stereotype.Component;

/**
 * Application pre-loader (add to vm args: -Djavafx.preloader=com.ibasco.sourcebuddy.Preloader to activate)
 *
 * @author Rafael Ibasco
 */
@Component
public class Preloader extends javafx.application.Preloader {

    private static final Logger log = LoggerFactory.getLogger(Preloader.class);

    private static Stage stage;

    private static ViewManager viewManager;

    private static PreloadModel preloadModel;

    @Override
    public void init() throws Exception {
        //Run spring application
        SpringApplicationBuilder app = new SpringApplicationBuilder()
                .sources(Preloader.class, Bootstrap.class)
                .banner(new SourceBuddyBanner())
                .bannerMode(Banner.Mode.CONSOLE)
                .logStartupInfo(true)
                .registerShutdownHook(true);
        app.run(getParameters().getRaw().toArray(new String[0]));
    }

    @Override
    public void start(Stage stage) throws Exception {
        Preloader.stage = stage;
        Parent rootNode = viewManager.loadView("preloader");
        Scene preloadScene = new Scene(rootNode);
        stage.setScene(preloadScene);
        stage.centerOnScreen();
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setTitle("Starting...");
        stage.setResizable(false);
        stage.getIcons().add(ResourceUtil.loadIcon(Icons.APP_ICON));
        stage.show();
        stage.toFront();
        GuiHelper.moveStageOnDrag(preloadScene);
    }

    @Override
    public void handleApplicationNotification(PreloaderNotification info) {
        if (info instanceof ProgressNotification) {
            ProgressNotification pn = (ProgressNotification) info;
            preloadModel.setProgress(pn.getProgress());
        } else if (info instanceof InfoNotification) {
            preloadModel.setMessage(((InfoNotification) info).getMessage());
        }
    }

    @Override
    public void handleStateChangeNotification(StateChangeNotification evt) {
        switch (evt.getType()) {
            case BEFORE_LOAD:
                break;
            case BEFORE_START:
                stage.hide();
                break;
            case BEFORE_INIT:
                break;
        }
    }

    @Autowired
    public void setViewManager(ViewManager viewManager) {
        Preloader.viewManager = viewManager;
    }

    @Autowired
    public void setPreloadModel(PreloadModel preloadModel) {
        Preloader.preloadModel = preloadModel;
    }
}
