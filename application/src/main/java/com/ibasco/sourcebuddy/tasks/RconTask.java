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

    public RconTask(ManagedServer managedServer, String command) {
        this.managedServer = Check.requireNonNull(managedServer, "Managed server argument is null");
        this.command = Check.requireNonBlank(command, "Command is empty");
    }

    @Override
    protected String process() throws Exception {
        updateTitle("Executing RCON Request");
        updateMessage("Command '%s'", command);
        return rconService.tryExecute(managedServer, command).join();
    }

    @Autowired
    public void setRconService(RconService rconService) {
        this.rconService = rconService;
    }
}
