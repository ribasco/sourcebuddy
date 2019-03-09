package com.ibasco.sourcebuddy.sourcebuddy;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.net.URL;

@SpringBootApplication
public class SourcebuddyApplication extends Application {

    private ConfigurableApplicationContext context;

    private Parent rootNode;

    @Override
    public void init() throws Exception {
        SpringApplicationBuilder builder = new SpringApplicationBuilder(SourcebuddyApplication.class);
        context = builder.run(getParameters().getRaw().toArray(new String[0]));
        URL resource = getClass().getResource("/main.fxml");
        FXMLLoader loader = new FXMLLoader(resource);
        loader.setControllerFactory(context::getBean);
        rootNode = loader.load();
    }

    @Override
    public void start(Stage stage) throws Exception {
        Rectangle2D visualBounds = Screen.getPrimary().getVisualBounds();
        double width = visualBounds.getWidth() / 2;
        double height = visualBounds.getHeight() / 2;
        stage.setScene(new Scene(rootNode, width, height));
        stage.centerOnScreen();
        stage.show();
    }

    @Override
    public void stop() throws Exception {
        context.close();
    }
}
