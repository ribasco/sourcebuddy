package com.ibasco.sourcebuddy.controllers;

import com.ibasco.agql.core.exceptions.ReadTimeoutException;
import static com.ibasco.sourcebuddy.constants.RegEx.IP_REGEX_VALIDATION;
import com.ibasco.sourcebuddy.gui.listeners.CopyPasteIPChangeListener;
import com.ibasco.sourcebuddy.model.AppModel;
import com.ibasco.sourcebuddy.service.ServerManager;
import com.ibasco.sourcebuddy.service.SourceServerService;
import com.ibasco.sourcebuddy.util.Predicates;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.validation.ValidationSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletionException;

public class AddServerController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(AddServerController.class);

    private ServerManager serverManager;

    private AppModel appModel;

    private SourceServerService sourceServerService;

    @FXML
    private Button btnAddServer;

    @FXML
    private TextField tfIpAddress;

    private ValidationSupport validationSupport = new ValidationSupport();

    @FXML
    private TextField tfPort;

    @FXML
    private ProgressIndicator piAddServer;

    @FXML
    private Label lblMessage;

    @FXML
    private Button btnClear;

    @Override
    public void initialize(Stage stage, Node rootNode) {
        log.debug("Initialized add server controller: {}", serverManager);
        btnAddServer.setOnAction(this::addNewServer);
        btnClear.setOnAction(this::clearInput);
        tfIpAddress.textProperty().addListener(new CopyPasteIPChangeListener(this::handleCopyPasteEvent));
        //validationSupport.registerValidator(tfIpAddress, Validator.createEmptyValidator("IP address is required", Severity.ERROR));
        //validationSupport.registerValidator(tfIpAddress,  Validator.createRegexValidator("Invalid IP Address", IP_REGEX_VALIDATION, Severity.ERROR));
        //validationSupport.registerValidator(tfPort,  Validator.createEmptyValidator("Port cannot be empty"));
        //validationSupport.registerValidator(tfPort, Validator.createPredicateValidator(Predicates::isValidPort, "Invalid port"));

        /*ValidationDecoration iconDecorator = new GraphicValidationDecoration();
        ValidationDecoration cssDecorator = new StyleClassValidationDecoration();
        ValidationDecoration compoundDecorator = new CompoundValidationDecoration(cssDecorator, iconDecorator);
        validationSupport.setValidationDecorator(compoundDecorator);
        validationSupport.initInitialDecoration();*/
    }

    private void handleCopyPasteEvent(String[] values) {
        clearInput(null);
        tfIpAddress.setText(values[0]);
        tfPort.setText(values[1]);
        tfPort.requestFocus();
        tfPort.selectEnd();
    }

    private void clearInput(ActionEvent actionEvent) {
        tfIpAddress.clear();
        tfPort.clear();
        lblMessage.setText(null);
        tfIpAddress.requestFocus();
    }

    private boolean validate() {
        lblMessage.setVisible(true);

        String ipAddress = tfIpAddress.getText();
        String port = tfPort.getText();

        String errorStyle = "-fx-text-fill: rgb(255,111,94); -fx-font-size: 14px";

        if (StringUtils.isBlank(ipAddress)) {
            lblMessage.setText("IP Address is empty");
            lblMessage.setStyle(errorStyle);
            return false;
        }

        if (!ipAddress.matches(IP_REGEX_VALIDATION)) {
            lblMessage.setText("Invalid IP Address");
            lblMessage.setStyle(errorStyle);
            return false;
        }

        if (!Predicates.isValidPort(port)) {
            lblMessage.setText("Invalid Port");
            lblMessage.setStyle(errorStyle);
            return false;
        }

        if (sourceServerService.exists(new InetSocketAddress(ipAddress, Integer.valueOf(port)))) {
            lblMessage.setText("Server is already in the database");
            lblMessage.setStyle(errorStyle);
            return false;
        }

        lblMessage.setStyle(null);
        lblMessage.setText(null);
        return true;
    }

    private void addNewServer(ActionEvent actionEvent) {
        btnAddServer.setDisable(true);
        piAddServer.setVisible(true);

        if (!validate()) {
            btnAddServer.setDisable(false);
            piAddServer.setVisible(false);
            return;
        }

        String ipAddress = tfIpAddress.getText();
        int port = Integer.valueOf(tfPort.getText().trim());
        InetSocketAddress address = new InetSocketAddress(ipAddress, port);

        sourceServerService.findServerDetails(address)
                .whenComplete((serverDetails, ex) -> {
                    try {
                        if (ex != null) {
                            log.error("Error retrieving server details for address: " + address, ex);
                            return;
                        }
                        log.debug("Got server details : {}. Saving to the database", serverDetails);
                        sourceServerService.save(serverDetails);
                        log.debug("Successfully added server {} to the repository", serverDetails);
                    } finally {
                        Platform.runLater(() -> {
                            piAddServer.setVisible(false);
                            btnAddServer.setDisable(false);
                            if (ex != null) {
                                lblMessage.setStyle("-fx-text-fill: rgb(255,111,94); -fx-font-size: 14px");
                                lblMessage.setWrapText(true);
                                String msg = "";
                                if (ex instanceof CompletionException) {
                                    if (ex.getCause() instanceof ReadTimeoutException) {
                                        msg = "Timed out";
                                    } else {
                                        msg = ex.getCause().getMessage();
                                    }
                                } else {
                                    msg = ex.getMessage();
                                }
                                lblMessage.setText("Error: " + msg);
                            } else {
                                lblMessage.setStyle("-fx-text-fill: green; -fx-font-size: 14px");
                                lblMessage.setText("Successfully added server: " + serverDetails.getName());
                            }

                        });
                    }
                });
    }

    @Autowired
    public void setServerManager(ServerManager serverManager) {
        this.serverManager = serverManager;
    }

    @Autowired
    public void setAppModel(AppModel appModel) {
        this.appModel = appModel;
    }

    @Autowired
    public void setSourceServerService(SourceServerService sourceServerService) {
        this.sourceServerService = sourceServerService;
    }
}
