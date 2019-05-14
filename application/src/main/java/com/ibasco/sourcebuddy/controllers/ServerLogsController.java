package com.ibasco.sourcebuddy.controllers;

import com.ibasco.agql.protocols.valve.source.query.logger.SourceLogEntry;
import com.ibasco.agql.protocols.valve.source.query.logger.SourceLogListenService;
import com.ibasco.sourcebuddy.domain.ManagedServer;
import com.ibasco.sourcebuddy.model.AppModel;
import com.ibasco.sourcebuddy.service.RconService;
import com.ibasco.sourcebuddy.tasks.RconTask;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.stage.Stage;
import org.fxmisc.richtext.StyledTextArea;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

public class ServerLogsController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(ServerLogsController.class);

    @FXML
    private StyledTextArea taLogs;

    private AppModel appModel;

    private RconService rconService;

    private SourceLogListenService sourceLogListenService;

    private Map<ManagedServer, Boolean> monitoredServers = new HashMap<>();

    @Override
    public void initialize(Stage stage, Node rootNode) {
        appModel.selectedManagedServerProperty().addListener(this::updateStateOnSelection);
    }

    private void updateStateOnSelection(ObservableValue<? extends ManagedServer> observable, ManagedServer oldValue, ManagedServer newValue) {
        if (newValue != null) {
            taLogs.clear();
            if (!isMonitoring(newValue)) {
                initLogService(newValue);
            }
        }
    }

    private boolean isMonitoring(ManagedServer server) {
        Boolean value = monitoredServers.get(server);
        return value != null && value;
    }

    private void initLogService(ManagedServer server) {
        String logCommand = "logaddress_add " + sourceLogListenService.getListenAddress().getAddress().getHostAddress() + ":" + sourceLogListenService.getListenAddress().getPort();

        RconTask rconTask = springHelper.getBean(RconTask.class, server, logCommand);
        taskManager.run(rconTask)
                .whenComplete((result, ex) -> {
                    Platform.runLater(() -> {
                        if (ex != null) {
                            log.error("RCON Error", ex);
                            return;
                        }
                        try {
                            taLogs.appendText("Now listening for logs for server: " + server + "\n");
                            sourceLogListenService.setLogEventCallback(this::onLogReceive);
                            log.info("Attempting to listen to server logs on {}", sourceLogListenService.getListenAddress());
                            sourceLogListenService.listen();
                            log.info("Listening for server log events : {}", sourceLogListenService.getListenAddress());
                            monitoredServers.putIfAbsent(server, true);
                        } catch (InterruptedException e) {
                            log.error("Error during listen service initialization", e);
                        }
                    });
                });
    }

    private void onLogReceive(SourceLogEntry sourceLogEntry) {
        //log.info("Received log entry from: {}", sourceLogEntry);
        ManagedServer managedServer = appModel.getSelectedManagedServer();
        if (managedServer == null) {
            return;
        }
        String msg = String.format("[%s] %s\n", sourceLogEntry.getSourceAddress(), sourceLogEntry.getMessage());
        Platform.runLater(() -> {
            taLogs.appendText(msg);
            taLogs.requestFollowCaret();
        });

    }

    @Autowired
    public void setAppModel(AppModel appModel) {
        this.appModel = appModel;
    }

    @Autowired
    public void setRconService(RconService rconService) {
        this.rconService = rconService;
    }

    @Autowired
    public void setSourceLogListenService(SourceLogListenService sourceLogListenService) {
        this.sourceLogListenService = sourceLogListenService;
    }
}
