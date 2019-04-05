package com.ibasco.sourcebuddy.controllers;

import com.ibasco.sourcebuddy.model.PreloadModel;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
public class PreloadController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(PreloadController.class);

    @FXML
    private Label lblProgressValue;

    @FXML
    private ProgressBar pbPreload;

    @FXML
    private Label lblProgressText;

    private PreloadModel preloadModel;

    @Override
    public void initialize(Stage stage, Node rootNode) {
        lblProgressText.textProperty().bind(preloadModel.messageProperty());
        StringBinding sb = Bindings.createStringBinding(() -> {
            double value = preloadModel.getProgress();
            if (value < 0)
                return "";
            return String.format("%.2f%%", value);
        }, preloadModel.progressProperty());
        lblProgressValue.textProperty().bind(sb);
        pbPreload.progressProperty().bind(preloadModel.progressProperty());
    }

    @Autowired
    public void setPreloadModel(PreloadModel preloadModel) {
        this.preloadModel = preloadModel;
    }
}
