package com.ibasco.sourcebuddy;

import com.ibasco.sourcebuddy.components.SpringHelper;
import com.ibasco.sourcebuddy.components.ViewManager;
import com.ibasco.sourcebuddy.constants.Beans;
import com.ibasco.sourcebuddy.constants.Icons;
import com.ibasco.sourcebuddy.constants.Views;
import com.ibasco.sourcebuddy.util.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.application.Preloader;
import javafx.beans.value.ChangeListener;
import javafx.concurrent.Worker;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.dockfx.DockPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;

import java.net.URL;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

@SpringBootApplication
public class Bootstrap extends Application implements ApplicationContextAware {

    private static final Logger log = LoggerFactory.getLogger(Bootstrap.class);

    private static ConfigurableApplicationContext context;

    private static ViewManager viewManager;

    private static ExecutorService taskExecutorService;

    private static SpringHelper springHelper;

    private UncaughtExceptionHandler exceptionHandler = new UncaughtExceptionHandler();

    /**
     * Only start pre-load task execution after auto-wiring has completed
     *
     * @throws InterruptedException
     *         Thrown when the pre-load operation has been interrupted by an external process
     */
    void preloadTasks() throws InterruptedException {
        log.info("Checking for preload tasks available");
        Map<String, PreloadTask> tasks = context.getBeansOfType(PreloadTask.class);

        log.info("Found {} pre-load tasks", tasks.size());

        for (Map.Entry<String, PreloadTask> task : tasks.entrySet()) {
            log.info("> Executing pre-load task: {}", task.getKey());
            executePreloadTaskAndWait(task.getValue());
        }
    }

    /**
     * Application initialization stuff
     *
     * @throws Exception
     *         thown when error during initialization occurs
     */
    @Override
    public void init() throws Exception {
        boolean preloaderDisabled = context == null;

        log.info("==============================================================================================");
        log.info("Bootstrap :: init() (Pre-loader status: {})", preloaderDisabled ? "Disabled" : "Enabled");
        log.info("==============================================================================================");

        if (preloaderDisabled) {
            //Run spring application
            SpringApplicationBuilder app = new SpringApplicationBuilder()
                    .sources(Bootstrap.class)
                    .banner(new SourceBuddyBanner())
                    .bannerMode(Banner.Mode.CONSOLE)
                    .logStartupInfo(true)
                    .registerShutdownHook(true);
            app.run(getParameters().getRaw().toArray(new String[0]));
        }

        updateMessage("Initializing spring context and components");

        //Application level stuff
        Thread.setDefaultUncaughtExceptionHandler(exceptionHandler);

        if (!preloaderDisabled) {
            //Start preloading
            preloadTasks();
        }
    }

    @Override
    public void start(Stage stage) throws Exception {
        log.info("==============================================================================================");
        log.info("Bootstrap :: start() : {}", viewManager);
        log.info("==============================================================================================");

        Font appFont = Font.loadFont(ResourceUtil.loadResourceAsStream("/fonts/consola.ttf"), 12);
        Parent rootNode = viewManager.loadView(Views.MAIN);
        springHelper.registerBean(Beans.PRIMARY_STAGE, stage);
        springHelper.registerSingleton("appFont", appFont);

        log.debug("Bootstrap :: Initializing primary stage and scene");
        Scene scene = new Scene(rootNode);
        URL res = ResourceUtil.loadResource("/styles/default.css");
        scene.getStylesheets().add(res.toExternalForm());

        stage.getIcons().add(ResourceUtil.loadIcon(Icons.APP_ICON));
        stage.setTitle(StringUtils.defaultString(getClass().getPackage().getImplementationTitle(), "Source Buddy"));
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.toFront();
        stage.setWidth(1280);
        stage.setHeight(720);
        stage.show();

        log.debug("Bootstrap :: Primary stage and scene initialized");

        // test the look and feel with both Caspian and Modena
        Application.setUserAgentStylesheet(Application.STYLESHEET_MODENA);
        DockPane.initializeDefaultUserAgentStylesheet();
    }

    private void executePreloadTaskAndWait(PreloadTask task) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        ChangeListener<Worker.State> taskListener = (observable, oldValue, newValue) -> {
            switch (newValue) {
                case CANCELLED:
                case FAILED:
                    log.error("Preload task failed :: " + task.getClass().getSimpleName(), task.getException());
                case SUCCEEDED:
                    latch.countDown();
                    break;
            }
        };
        try {
            task.stateProperty().addListener(taskListener);
            taskExecutorService.execute(task);
            latch.await();
        } finally {
            Platform.runLater(() -> task.stateProperty().removeListener(taskListener));
        }
    }

    void updateMessage(String message) {
        notifyPreloader(new InfoNotification(message));
    }

    void updateProgress(double value) {
        notifyPreloader(new Preloader.ProgressNotification(value));
    }

    @Override
    public void stop() throws Exception {
        log.info("Stopping application");
        context.close();
    }

    @Autowired
    public void setTaskExecutorService(ExecutorService taskExecutorService) {
        Bootstrap.taskExecutorService = taskExecutorService;
    }

    @Autowired
    public void setViewManager(ViewManager viewManager) {
        Bootstrap.viewManager = viewManager;
    }

    @Autowired
    public void setSpringHelper(SpringHelper springHelper) {
        Bootstrap.springHelper = springHelper;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Bootstrap.context = (ConfigurableApplicationContext) applicationContext;
    }
}

