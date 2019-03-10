package com.ibasco.sourcebuddy.sourcebuddy;

import com.ibasco.sourcebuddy.sourcebuddy.config.AppConfig;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.net.URL;

@SpringBootApplication
public class Bootstrap extends Application {

    private static final Logger log = LoggerFactory.getLogger(Bootstrap.class);

    private ConfigurableApplicationContext context;

    private Parent rootNode;

    @Override
    public void init() throws Exception {
        SpringApplicationBuilder builder = new SpringApplicationBuilder(Bootstrap.class).sources(AppConfig.class);
        context = builder.run(getParameters().getRaw().toArray(new String[0]));
        URL resource = getClass().getResource("/main.fxml");
        FXMLLoader loader = new FXMLLoader(resource);
        loader.setControllerFactory(context::getBean);
        rootNode = loader.load();
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("Source Buddy");
        stage.setScene(new Scene(rootNode));
        stage.centerOnScreen();
        stage.show();
    }

    @Override
    public void stop() throws Exception {
        log.info("Stopping application");
        context.close();
    }
}
