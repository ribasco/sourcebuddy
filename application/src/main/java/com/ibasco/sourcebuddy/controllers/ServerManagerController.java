package com.ibasco.sourcebuddy.controllers;

import com.ibasco.agql.protocols.valve.source.query.client.SourceRconClient;
import com.ibasco.agql.protocols.valve.source.query.exceptions.RconNotYetAuthException;
import com.ibasco.agql.protocols.valve.source.query.logger.SourceLogEntry;
import com.ibasco.agql.protocols.valve.source.query.logger.SourceLogListenService;
import com.ibasco.sourcebuddy.domain.ServerDetails;
import com.ibasco.sourcebuddy.model.ServerDetailsModel;
import javafx.scene.Node;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Controller
public class ServerManagerController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(ServerManagerController.class);

    private SourceRconClient rconClient;

    private ServerDetailsModel serverDetailsModel;

    private SourceLogListenService sourceLogListenService;

    @Override
    public void initialize(Stage stage, Node rootNode) {

    }

    private void authenticateRcon() {
        ServerDetails info = serverDetailsModel.getServerSelectionModel().getSelectedItem();

        if (rconClient.isAuthenticated(info.getAddress())) {
            log.warn("Address {} is already authenticated", info.getAddress());
            return;
        }

        log.info("Authenticating address: {}", info.getAddress());

        TextInputDialog passDialog = new TextInputDialog();
        passDialog.setHeaderText("Enter rcon password");
        passDialog.setContentText("Password");
        Optional<String> password = passDialog.showAndWait();

        if (password.isPresent()) {
            rconClient.authenticate(info.getAddress(), password.get()).whenComplete((status, throwable) -> {
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
        ServerDetails selectedServer = serverDetailsModel.getServerSelectionModel().getSelectedItem();
        if (selectedServer == null)
            return CompletableFuture.failedFuture(new IllegalStateException("No server selected"));

        try {
            return rconClient.execute(selectedServer.getAddress(), command).thenApply(s -> {
                /*Platform.runLater(() -> {
                    synchronized (logLock) {
                        taServerLog.appendText(s);
                    }
                });
                rconHistory.push(command);*/
                return s;
            });
        } catch (RconNotYetAuthException e) {
            //not yet authenticated
            log.error("You are not yet authenticated");
            authenticateRcon();
            return CompletableFuture.failedFuture(e);
        }
    }

    @Autowired
    public void setRconClient(SourceRconClient rconClient) {
        this.rconClient = rconClient;
    }

    @Autowired
    public void setServerDetailsModel(ServerDetailsModel serverDetailsModel) {
        this.serverDetailsModel = serverDetailsModel;
    }

    @Autowired
    public void setSourceLogListenService(SourceLogListenService sourceLogListenService) {
        this.sourceLogListenService = sourceLogListenService;
    }
}
