package com.ibasco.sourcebuddy.controllers;

import com.ibasco.agql.protocols.valve.source.query.logger.SourceLogEntry;
import com.ibasco.agql.protocols.valve.source.query.logger.SourceLogListenService;
import com.ibasco.sourcebuddy.domain.ServerDetails;
import com.ibasco.sourcebuddy.exceptions.NotAuthenticatedException;
import com.ibasco.sourcebuddy.model.ServerDetailsModel;
import com.ibasco.sourcebuddy.service.RconService;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;
import org.controlsfx.control.textfield.CustomTextField;
import org.controlsfx.control.textfield.TextFields;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.StyledTextArea;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class ServerManagerController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(ServerManagerController.class);

    private ServerDetailsModel serverDetailsModel;

    private SourceLogListenService sourceLogListenService;

    private RconService rconService;

    @FXML
    private StyledTextArea taRconLog;

    @FXML
    private CustomTextField tfRconCommand;

    @Override
    public void initialize(Stage stage, Node rootNode) {
        taRconLog.setParagraphGraphicFactory(LineNumberFactory.get(taRconLog));
        TextFields.bindAutoCompletion(tfRconCommand, "status", "history", "cvarlist");
    }

    private void authenticateRcon() {
        //ServerDetails info = serverDetailsModel.getServerSelectionModel().getSelectedItem();

        ServerDetails info = new ServerDetails();

        if (rconService.isAuthenticated(info.getAddress())) {
            log.warn("Address {} is already authenticated", info.getAddress());
            return;
        }

        log.info("Authenticating address: {}", info.getAddress());

        TextInputDialog passDialog = new TextInputDialog();
        passDialog.setHeaderText("Enter rcon password");
        passDialog.setContentText("Password");
        Optional<String> password = passDialog.showAndWait();

        if (password.isPresent()) {
            rconService.authenticate(info.getAddress(), password.get()).whenComplete((status, throwable) -> {
                if (throwable != null) {
                    log.error("Problem authenticating with server {}", info.getAddress());
                    return;
                }
                if (!status.isAuthenticated()) {
                    log.warn("RCON Authentication failed for server {} (Reason: {})", info.getAddress(), status.getReason());
                    return;
                }

                log.info("Successfully authenticated with server: {}", info.getAddress());

                String logCommand = "logaddress_add " + sourceLogListenService.getListenAddress().getAddress().getHostAddress() + ":" + sourceLogListenService.getListenAddress().getPort();

                //Start listening to logs
                executeRconCommand(logCommand).thenAccept(s -> {
                    try {
                        sourceLogListenService.setLogEventCallback(this::onLogReceive);
                        log.info("Attempting to listen to server logs on {}", sourceLogListenService.getListenAddress());
                        sourceLogListenService.listen();
                        log.info("Listening for server log events : {}", sourceLogListenService.getListenAddress());
                    } catch (InterruptedException e) {
                        log.error("Error during listen service initialization", e);
                    }
                });
            });
            log.info("Authenticating with server: {}", info.getAddress());
        }
    }

    private void onLogReceive(SourceLogEntry sourceLogEntry) {
        /*Platform.runLater(() -> {
            synchronized (logLock) {
                taServerLog.appendText(sourceLogEntry.getMessage() + "\n");
            }
        });*/
    }

    private CompletableFuture<String> executeRconCommand(String command) {
        if (serverDetailsModel.getSelectedServers().isEmpty())
            return CompletableFuture.completedFuture(null);

        ServerDetails selectedServer = serverDetailsModel.getSelectedServers().get(0);
        if (selectedServer == null)
            return CompletableFuture.failedFuture(new IllegalStateException("No server selected"));

        try {
            return rconService.execute(selectedServer.getAddress(), command).thenApply(s -> {
                /*Platform.runLater(() -> {
                    synchronized (logLock) {
                        taServerLog.appendText(s);
                    }
                });
                rconHistory.push(command);*/
                return s;
            });
        } catch (NotAuthenticatedException e) {
            //not yet authenticated
            log.error("You are not yet authenticated");
            authenticateRcon();
            return CompletableFuture.failedFuture(e);
        }
    }

    @Autowired
    public void setServerDetailsModel(ServerDetailsModel serverDetailsModel) {
        this.serverDetailsModel = serverDetailsModel;
    }

    @Autowired
    public void setSourceLogListenService(SourceLogListenService sourceLogListenService) {
        this.sourceLogListenService = sourceLogListenService;
    }

    @Autowired
    public void setRconService(RconService rconService) {
        this.rconService = rconService;
    }
}
