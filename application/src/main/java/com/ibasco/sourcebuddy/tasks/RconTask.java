package com.ibasco.sourcebuddy.tasks;

import com.ibasco.sourcebuddy.domain.ManagedServer;
import com.ibasco.sourcebuddy.service.RconService;
import com.ibasco.sourcebuddy.util.Check;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;
import org.springframework.context.annotation.Scope;

@Scope(SCOPE_PROTOTYPE)
public class RconTask extends BaseTask<String> {

    private static final Logger log = LoggerFactory.getLogger(RconTask.class);

    private RconService rconService;

    private ManagedServer managedServer;

    private String command;

    private static final int MAX_RETRIES = 3;

    public RconTask(ManagedServer managedServer, String command) {
        this.managedServer = Check.requireNonNull(managedServer, "Managed server argument is null");
        this.command = Check.requireNonBlank(command, "Command is empty");
    }

    @Override
    protected String process() throws Exception {
        /*updateTitle("Executing RCON Request");
        for (int retryCount = 1; retryCount <= MAX_RETRIES; retryCount++) {
            try {
                if (!rconService.isAuthenticated(managedServer)) {
                    log.info("RconTask :: Authenticating with server '{}' (Attempts: {})", managedServer.getServerDetails(), retryCount);
                    updateMessage("Authenticating with server: %s (Attempts: %d)", managedServer.getServerDetails(), retryCount);
                    RconStatus authStatus = rconService.authenticate(managedServer).join();
                    if (authStatus == null || !authStatus.isAuthenticated())
                        continue;
                }
                log.info("RconTask :: Executing command '{}' on server '{}' (Attempts: {})", command, managedServer.getServerDetails(), retryCount);
                return rconService.execute(managedServer, command).join();
            } catch (ReadTimeoutException | CompletionException | NotAuthenticatedException ex) {
                log.debug("RconTask :: Server authentication failed: " + managedServer.getServerDetails(), ex);
            }
        }
        RconStatus authStatus = rconService.getStatus(managedServer);
        throw new NotAuthenticatedException(String.format("Failed to authenticate with server (Reason: %s)", authStatus != null ? authStatus.getReason() : "N/A"));*/
        return rconService.tryExecute(managedServer, command).join();
    }

    @Autowired
    public void setRconService(RconService rconService) {
        this.rconService = rconService;
    }
}
