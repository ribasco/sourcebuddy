package com.ibasco.sourcebuddy.controllers;

import com.ibasco.agql.protocols.valve.source.query.logger.SourceLogListenService;
import com.ibasco.sourcebuddy.components.CommandHistory;
import com.ibasco.sourcebuddy.components.ConsoleBuffer;
import com.ibasco.sourcebuddy.domain.ManagedServer;
import com.ibasco.sourcebuddy.exceptions.NotAuthenticatedException;
import com.ibasco.sourcebuddy.model.AppModel;
import com.ibasco.sourcebuddy.service.RconService;
import com.ibasco.sourcebuddy.service.ServerManager;
import com.ibasco.sourcebuddy.tasks.RconTask;
import com.ibasco.sourcebuddy.util.Check;
import com.jfoenix.controls.JFXSpinner;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.textfield.CustomTextField;
import org.fxmisc.richtext.CodeArea;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

public class RconController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(RconController.class);

    @FXML
    private CodeArea taRconLog;

    @FXML
    private CustomTextField tfRconCommand;

    private AppModel appModel;

    private SourceLogListenService sourceLogListenService;

    private RconService rconService;

    private ServerManager serverManager;

    private CommandHistory commandHistory;

    private Map<ManagedServer, ConsoleBufferChangeListener> consoleBufferListeners = new HashMap<>();

    @FXML
    private JFXSpinner piRcon;

    private class ConsoleBufferChangeListener implements ChangeListener<String> {

        private ManagedServer managedServer;

        private ConsoleBuffer consoleBuffer;

        private ConsoleBufferChangeListener(ManagedServer managedServer, int limit) {
            this.managedServer = Check.requireNonNull(managedServer, "Managed server cannot be null");
            this.consoleBuffer = new ConsoleBuffer(limit);
        }

        @Override
        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
            if (managedServer == null)
                return;
            if (newValue != null) {
                consoleBuffer.setBuffer(newValue);
            }
        }

        private String getBuffer() {
            return consoleBuffer.getBuffer();
        }
    }

    @Override
    public void initialize(Stage stage, Node rootNode) {
        //taRconLog.setParagraphGraphicFactory(LineNumberFactory.get(taRconLog));
        tfRconCommand.setOnKeyPressed(this::handleRconComamndKeyEvents);
        //TextFields.bindAutoCompletion(tfRconCommand, "status", "history", "cvarlist");
        appModel.selectedManagedServerProperty().addListener(this::updateStateOnSelection);

        piRcon.setVisible(false);
        piRcon.setManaged(false);
    }

    private void updateStateOnSelection(ObservableValue<? extends ManagedServer> observable, ManagedServer oldValue, ManagedServer newValue) {
        if (oldValue != null) {
            //save the rcon history of the previously selected server
            ConsoleBufferChangeListener listener = getConsoleBufferListener(oldValue);
            taRconLog.textProperty().removeListener(listener);
        }
        if (newValue != null) {
            //Restore the rcon history (if applicable) of the newly selected server
            ConsoleBufferChangeListener listener = getConsoleBufferListener(newValue);
            taRconLog.textProperty().addListener(listener);
            taRconLog.clear();
            if (listener.getBuffer() != null) {
                taRconLog.appendText(listener.getBuffer());
                taRconLog.requestFollowCaret();
            }
        }
    }

    private ConsoleBufferChangeListener getConsoleBufferListener(ManagedServer server) {
        return consoleBufferListeners.computeIfAbsent(server, managedServer -> {
            log.info("Computing new string buffer for {}", server.getServerDetails());
            return new ConsoleBufferChangeListener(server, 512000);
        });
    }

    private void handleRconComamndKeyEvents(KeyEvent keyEvent) {
        if (KeyCode.ENTER.equals(keyEvent.getCode())) {
            String command = tfRconCommand.getText();
            if (StringUtils.isBlank(command)) {
                log.warn("No command entered");
                return;
            }
            ManagedServer managedServer = appModel.getSelectedManagedServer();
            if (managedServer == null) {
                log.warn("No managed server selected");
                return;
            }
            executeRcon(command, managedServer);
        } else if (KeyCode.UP.equals(keyEvent.getCode())) {
            String cmd = commandHistory.previous();
            log.debug("Previous: {}", cmd);
            if (cmd != null) {
                tfRconCommand.setText(cmd);
                tfRconCommand.requestFocus();
            }
            commandHistory.getEntries().forEach(e -> log.debug("\t{}", e));
        } else if (KeyCode.DOWN.equals(keyEvent.getCode())) {
            String cmd = commandHistory.next();
            log.debug("Next: {}", cmd);
            if (cmd != null) {
                tfRconCommand.setText(cmd);
                tfRconCommand.requestFocus();
            }
            commandHistory.getEntries().forEach(e -> log.debug("\t{}", e));
        } else if (keyEvent.isControlDown() && KeyCode.L.equals(keyEvent.getCode())) {
            taRconLog.clear();
        }
    }

    private void executeRcon(String command, ManagedServer managedServer) {
        if (managedServer == null) {
            log.warn("Server is null");
            return;
        }

        if (managedServer.getServerDetails().getSteamApp() == null) {
            log.warn("Steam app not available for {}", managedServer.getServerDetails());
            return;
        }

        RconTask rconTask = springHelper.getBean(RconTask.class, managedServer, command);

        ChangeListener<String> listener = (observable, oldValue, newValue) -> {
            if (newValue != null) {
                appendConsole(newValue);
            }
        };

        rconTask.messageProperty().addListener(listener);
        tfRconCommand.disableProperty().bind(rconTask.runningProperty());
        piRcon.visibleProperty().bind(rconTask.runningProperty());
        piRcon.managedProperty().bind(rconTask.runningProperty());

        taskManager.run(rconTask)
                .whenComplete((result, ex) -> {
                    Platform.runLater(() -> {
                        try {
                            if (ex != null) {
                                if (ex instanceof NotAuthenticatedException) {
                                    appendConsole("Error: Failed to authenticate with server: " + managedServer.getServerDetails());
                                } else {
                                    getNotificationManager().showError("An unexpected error has occured during request: " + ex.getMessage());
                                }
                                return;
                            }
                            commandHistory.add(command);
                            appendConsole(result);
                            tfRconCommand.clear();
                        } finally {
                            tfRconCommand.disableProperty().unbind();
                            piRcon.visibleProperty().unbind();
                            piRcon.managedProperty().unbind();
                            taRconLog.requestFollowCaret();
                            tfRconCommand.requestFocus();
                            rconTask.messageProperty().removeListener(listener);
                        }
                    });
                });
    }

    private void appendConsole(String message) {
        Runnable runnable = () -> taRconLog.appendText(message + "\n");
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(runnable);
        } else {
            runnable.run();
        }
    }

    @Autowired
    public void setAppModel(AppModel appModel) {
        this.appModel = appModel;
    }

    @Autowired
    public void setSourceLogListenService(SourceLogListenService sourceLogListenService) {
        this.sourceLogListenService = sourceLogListenService;
    }

    @Autowired
    public void setRconService(RconService rconService) {
        this.rconService = rconService;
    }

    @Autowired
    public void setServerManager(ServerManager serverManager) {
        this.serverManager = serverManager;
    }

    @Autowired
    public void setCommandHistory(CommandHistory commandHistory) {
        this.commandHistory = commandHistory;
    }
}
