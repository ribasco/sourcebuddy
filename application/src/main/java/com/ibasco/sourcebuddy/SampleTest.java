package com.ibasco.sourcebuddy;

import com.ibasco.sourcebuddy.controls.ProgressComboBox;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SampleTest extends Application {

    private static final Logger log = LoggerFactory.getLogger(SampleTest.class);

    @Override
    public void start(Stage primaryStage) throws Exception {
        ProgressComboBox<String> pcb = new ProgressComboBox<>();
        primaryStage.setScene(new Scene(new VBox(pcb)));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}